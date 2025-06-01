package cn.mrcode.dtdemo.a.service;

import cn.mrcode.dtdemo.a.repo.entity.TOrder;
import cn.mrcode.dtdemo.a.repo.mapper.TOrderMapper;
import cn.mrcode.dtdemo.a.sdk.StorageFeignClient;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private TOrderMapper orderMapper;

    @Autowired
    private StorageFeignClient storageFeignClient;

    @GlobalTransactional(timeoutMills = 1000 * 60 * 30)  // 开启全局事务
    @Override
    public void createOrder(String userId, String productId, Integer count) {

        // 1. 创建订单
        TOrder order = new TOrder();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setMoney(count * 100L);
        orderMapper.insert(order);

        // 2. 扣减库存（远程调用库存服务）
        storageFeignClient.decrease(productId, count);

        // 3. 模拟异常（测试回滚）
        if (count > 10) {
            throw new RuntimeException("订单金额过大，回滚");
        }
    }
}
