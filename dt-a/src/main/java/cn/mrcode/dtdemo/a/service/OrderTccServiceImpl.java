package cn.mrcode.dtdemo.a.service;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.mrcode.dtdemo.a.repo.entity.TOrder;
import cn.mrcode.dtdemo.a.repo.mapper.TOrderMapper;
import cn.mrcode.dtdemo.a.sdk.StorageFeignClient;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
public class OrderTccServiceImpl implements OrderTccService {
    @Autowired
    private TOrderMapper orderMapper;

    // 这个是库存 feign 客户端
    @Autowired
    private StorageFeignClient storageFeignClient;

    // 为了简单一点，悬挂/空回滚/冥等检查使用这个来实现
    // 正常情况下是使用一个本地消息表来存储，因为本地消息表可以持久化，而且可以和订单创建在同一个本地事务中
    // Seata 1.5.1+ 中已经内置这种方式了，不过需要提前创建表
    // 详细的可以查看这个官网文档：https://seata.apache.org/zh-cn/blog/seata-tcc-fence
    private static final Set<String> TX_STATE_MAP = new ConcurrentHashSet<>();

    @Override
    // 使用事物，是为了 订单和事物日志 在同一事物中
    @Transactional
    public boolean prepare(BusinessActionContext actionContext,
                           // 在实现类上添加该注解，才会自动将 标记的参数 存放在 actionContext 中，且会持久化下来，就可以传入到下一阶段了（比如 commit 阶段）
                           @BusinessActionContextParameter(paramName = "userId") String userId,
                           @BusinessActionContextParameter(paramName = "productId") String productId,
                           @BusinessActionContextParameter(paramName = "count") Integer count) {

        // 1. 悬挂问题检查
        String txId = actionContext.getXid();
        if (TX_STATE_MAP.contains(txId + "EMPTY_CANCEL")) {
            log.warn("存在空回滚记录，拒绝Try操作: {}", txId);
            throw new RuntimeException("存在空回滚记录");
        }

        // 2. 幂等性检查
        if (TX_STATE_MAP.contains(txId + "TRY")) {
            log.info("Try阶段已执行，直接返回: {}", txId);
            return true;
        }

        // 3. 创建订单（待确认状态）
        TOrder order = new TOrder();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setStatus(1);  // 待确认状态
        order.setMoney(count * 100L);
        orderMapper.insert(order);


        // 调用远程服务，扣减库存
        Boolean inventoryResult = storageFeignClient.decrease(txId, productId, count);
        if (inventoryResult == null || !inventoryResult) {
            throw new RuntimeException("库存冻结失败");
        }

        // 4. 保存事务日志
        TX_STATE_MAP.add(txId + "TRY");

        // 5. 将订单ID放入上下文
        actionContext.getActionContext().put("orderId", order.getId());
        // 这一步骤很重要，标记上下文已经更新过了，不然这里设置的 orderId 在 commit 阶段就看不到
        actionContext.setUpdated(true);


        log.info("订单Try阶段成功: {}, orderId: {}", txId, order.getId());
        return true;
    }

    @Override
    @Transactional
    public boolean commit(BusinessActionContext actionContext) {
        String txId = actionContext.getXid();

        // 1. 幂等性检查
        if (TX_STATE_MAP.contains(txId + "CONFIRM")) {
            log.info("Confirm阶段已执行: {}", txId);
            return true;
        }

        // 2. 获取订单ID
        Long orderId = actionContext.getActionContext("orderId", Long.class);
        TOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            // 无论是抛出异常还是返回 false 都会导致无限重试
            throw new RuntimeException("订单不存在: " + orderId);
//            return true;
        }

        // 3. 更新订单状态为已确认
        TOrder r = new TOrder();
        r.setId(orderId);
        r.setStatus(2);  // 已确认状态
        orderMapper.updateById(r);

        // 4. 保存事务日志
        TX_STATE_MAP.add(txId + "CONFIRM");

        log.info("订单Confirm阶段成功: {}, orderId: {}", txId, orderId);
        return true;
    }

    @Override
    @Transactional
    public boolean rollback(BusinessActionContext actionContext) {
        String txId = actionContext.getXid();

        // 1. 空回滚检查
        if (!TX_STATE_MAP.contains(txId + "TRY")) {
            if (!TX_STATE_MAP.contains(txId + "EMPTY_CANCEL")) {
                // 记录一次空回滚
                TX_STATE_MAP.add(txId + "EMPTY_CANCEL");
                log.warn("空回滚处理: {}", txId);
            }
        }

        // 2. 幂等性检查
        if (TX_STATE_MAP.contains(txId + "CANCEL")) {
            log.info("Cancel阶段已执行: {}", txId);
            return true;
        }

        // 3. 获取订单ID
        // 不能使用  (Long)actionContext.getActionContext().get("orderId");
        // 猜测应该是使用 json 反序列化的，所以这个类型不一定是 Long, 比如在第一阶段存入 17，那么这里获取到的就有可能是 Int 类型
        Long orderId = actionContext.getActionContext("orderId", Long.class);
        TOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            // 这种不存在一直抛出异常， 或则返回 False 都会导致重复的 重试
//            throw new RuntimeException("订单不存在: " + orderId);
            log.warn("订单不存在: {}", orderId);
            return true;
        }

        // 4. 更新订单状态为已取消
        TOrder r = new TOrder();
        r.setId(orderId);
        r.setStatus(3);
        orderMapper.updateById(r);

        // 4. 保存事务日志
        TX_STATE_MAP.add(txId + "CANCEL");

        log.info("订单Cancel阶段成功: {}, orderId: {}", txId, orderId);
        return true;
    }
}
