package cn.mrcode.dtdemo.b.web;

import cn.mrcode.dtdemo.b.service.InventoryTccService;
import io.seata.core.context.RootContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private InventoryTccService inventoryTccService;

    @PostMapping("/decrease")
    public Boolean decrease(
            @RequestParam("txId") String txId,
            @RequestParam("productId") String productId,
            @RequestParam("count") Integer count
    ) {
        // 绑定全局事务ID
        RootContext.bind(txId);
        // 取消绑定不用自己做，因为在全局事务完成后会自动解绑
        // RootContext.unbind();
        // 这里后面不使用 actionContext，就可以不传递，因为可以使用 @BusinessActionContextParameter 注解将传递的参数存入 actionContext 中
//        BusinessActionContext actionContext = new BusinessActionContext();
//        actionContext.setXid(txId);
//        actionContext.setActionContext(new HashMap<>());
//        actionContext.getActionContext().put("productId", productId);
//        actionContext.getActionContext().put("count", count);
        try {
            return inventoryTccService.prepare(null, productId, count);
        } finally {
            RootContext.unbind();
        }
    }
}
