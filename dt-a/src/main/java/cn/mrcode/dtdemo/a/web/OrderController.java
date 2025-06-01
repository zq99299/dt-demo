package cn.mrcode.dtdemo.a.web;

import cn.mrcode.dtdemo.a.service.OrderService;
import cn.mrcode.dtdemo.a.web.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/order")
@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public R createOrder(String userId, String productId, Integer count) {
        orderService.createOrder(userId, productId, count);
        return R.success("OK");
    }
}
