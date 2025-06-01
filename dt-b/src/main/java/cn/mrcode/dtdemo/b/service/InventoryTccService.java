package cn.mrcode.dtdemo.b.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

@LocalTCC
public interface InventoryTccService {

    @TwoPhaseBusinessAction(
            name = "inventoryTccService",
            commitMethod = "commit",
            rollbackMethod = "rollback"
    )
    boolean prepare(
            BusinessActionContext actionContext,
            @BusinessActionContextParameter(paramName = "productId") String productId,
            @BusinessActionContextParameter(paramName = "count") Integer count
    );

    boolean commit(BusinessActionContext actionContext);

    boolean rollback(BusinessActionContext actionContext);
}
