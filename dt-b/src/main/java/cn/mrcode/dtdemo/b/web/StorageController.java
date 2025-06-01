package cn.mrcode.dtdemo.b.web;

import cn.mrcode.dtdemo.b.service.StorageService;
import cn.mrcode.dtdemo.b.web.dto.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @PostMapping("/decrease")
    public R<String> decrease(
            @RequestParam("productId") String productId,
            @RequestParam("count") Integer count
    ) {
        storageService.decrease(productId, count);
        return R.success("库存扣减成功");
    }
}
