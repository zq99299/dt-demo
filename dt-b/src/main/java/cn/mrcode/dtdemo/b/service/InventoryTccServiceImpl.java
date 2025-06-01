package cn.mrcode.dtdemo.b.service;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.mrcode.dtdemo.b.repo.entity.TStorage;
import cn.mrcode.dtdemo.b.repo.mapper.TStorageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
public class InventoryTccServiceImpl implements InventoryTccService {
    @Autowired
    private TStorageMapper storageMapper;
    // 为了简单一点，悬挂/空回滚/冥等检查使用这个来实现
    private static final Set<String> TX_STATE_MAP = new ConcurrentHashSet<>();

    @Override
    @Transactional // 每个方法需要使用本地事务注解
    public boolean prepare(BusinessActionContext actionContext,
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

        TStorage storage = getByProductId(productId);
        if (storage == null) {
            return true;
        }
        if (storage.getResidue() < count) {
            throw new RuntimeException("扣减库存失败，库存不足");
        }


        // 3. 冻结库存
        TStorage r = new TStorage();
        r.setId(storage.getId());
        // 从库存里面减掉
        r.setResidue(storage.getResidue() - count);
        // 给冻结库存加上
        r.setFrozen(storage.getFrozen() + count);
        storageMapper.updateById(r);

        // 4. 记录事务状态
        TX_STATE_MAP.add(txId + "TRY");

        log.info("库存Try阶段成功: {}, productId: {}, count: {}", txId, productId, count);
        return true;
    }

    private TStorage getByProductId(String productId) {
        LambdaQueryWrapper<TStorage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TStorage::getProductId, productId);
        TStorage storage = storageMapper.selectOne(queryWrapper);
        if (storage == null) {
            throw new RuntimeException("扣减库存失败，商品不存在");
//            return null;
        }
        return storage;
    }

    @Override
    @Transactional
    public boolean commit(BusinessActionContext actionContext) {
        // 1. 冥等检查
        String txId = actionContext.getXid();
        if (TX_STATE_MAP.contains(txId + "COMMIT")) {
            log.info("Confirm阶段已执行: {}", txId);
            return true;
        }

        String productId = (String) actionContext.getActionContext().get("productId");
        Integer count = (Integer) actionContext.getActionContext().get("count");

        // 2. 扣减冻结库存
        TStorage storage = getByProductId(productId);
        if (storage == null) {
            return true;
        }
        TStorage r = new TStorage();
        r.setId(storage.getId());
        // 从冻结库存里面减掉
        r.setFrozen(storage.getFrozen() - count);
        // 然后加到已使用上
        r.setUsed(storage.getUsed() + count);
        storageMapper.updateById(r);

        // 3. 记录事物日志
        TX_STATE_MAP.add(txId + "COMMIT");

        log.info("库存Confirm阶段成功: {}, productId: {}, count: {}", txId, productId, count);
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

        String productId = (String) actionContext.getActionContext().get("productId");
        if ("error".equals(productId)) {
            // 这里我不知道如何处理，暂时只能这种方式
            return true;
        }
        Integer count = (Integer) actionContext.getActionContext().get("count");

        // 3. 执行回滚
        TStorage storage = getByProductId(productId);
        if (storage == null) {
            return true;
        }
        TStorage r = new TStorage();
        r.setId(storage.getId());
        r.setFrozen(storage.getFrozen() - count);
        r.setResidue(storage.getResidue() + count);
        storageMapper.updateById(r);
        // 4. 记录事物日志
        TX_STATE_MAP.add(txId + "CANCEL");

        log.info("库存Cancel阶段成功: {}, productId: {}, count: {}", txId, productId, count);
        return true;
    }
}
