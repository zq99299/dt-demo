package cn.mrcode.dtdemo.a.service;

import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    // 这里依赖 TCC 的订单服务，而不是直接 mapper
    @Autowired
    private OrderTccService orderTccService;

    /**
     * 事务协调器
     */
    @GlobalTransactional(timeoutMills = 1000 * 60 * 30)  // 开启全局事务
    @Override
    public void createOrder(String userId, String productId, Integer count) {

        // 创建业务上下文, 因为后面的方法要依赖该上下文获取处理成功的订单 ID
        BusinessActionContext actionContext = new BusinessActionContext();
        // 1. 执行订单服务Try
        boolean orderPrepare = orderTccService.prepare(
                actionContext,
                userId,
                productId,
                count
        );

        if (!orderPrepare) {
            throw new RuntimeException("订单创建失败");
        }

        // 需要注意的是：try 成功之后，Confirm/Cancel 阶段对应的方法可能就会立即被调用
        // 所以说：代码到了这里，无论这里你端点还是不端点，都不会影响 commit 方法被回调
        // 如果执行成功，Seata会自动触发Confirm阶段
        // 如果失败，Seata会自动触发Cancel阶段
        // BusinessActionContextUtil.getContext() 这里获取不到是空的，但是在 OrderTccServiceImpl 里面可以获取到，但是又没有任何意义
        Long orderId = (Long) actionContext.getActionContext().get("orderId");
        log.info("订单创建成功，订单ID: {}", orderId);
    }
}
