package cn.mrcode.dtdemo.a.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

@LocalTCC // @LocalTCC 注解用来表示实现了二阶段提交的本地的 TCC 接口
public interface OrderTccService {
    // @TwoPhaseBusinessAction 表示了当前方法使用TCC模式管理事务提交
    @TwoPhaseBusinessAction(
            name = "orderTccService",  // 给当前事务注册了一个全局唯一的的 TCC bean name,应该就是 资源名称
            commitMethod = "commit",  // 提交方法名称，也就是该接口内的 commit 方法
            rollbackMethod = "rollback"
    )
    // try 阶段
    boolean prepare(
            BusinessActionContext actionContext,
            // 如果是对象也是同理，比如：
//            @BusinessActionContextParameter(paramName = "orderRequest") OrderRequest request
            // 该注解在接口上无效，它的作用是 把参数存储在 actionContext 中（会持久化下来，比如在 commit 阶段能获取到）
            @BusinessActionContextParameter(paramName = "userId") String userId,
            @BusinessActionContextParameter(paramName = "productId") String productId,
            @BusinessActionContextParameter(paramName = "count") Integer count
    );

    // 提交阶段
    boolean commit(BusinessActionContext actionContext);

    // 回滚阶段
    boolean rollback(BusinessActionContext actionContext);
}