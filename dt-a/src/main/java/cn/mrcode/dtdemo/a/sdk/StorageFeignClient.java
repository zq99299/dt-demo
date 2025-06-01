package cn.mrcode.dtdemo.a.sdk;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "storage-service")  // 对应库存服务的 spring.application.name
public interface StorageFeignClient {

    @PostMapping("/storage/decrease")
        // 对应库存服务的接口路径
    Boolean decrease(
            @RequestParam("txId") String txId,
            @RequestParam("productId") String productId,
            @RequestParam("count") Integer count
    );
}
