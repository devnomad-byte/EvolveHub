package org.evolve.common.base;

/**
 * 业务处理抽象父类
 * <p>
 * 定义统一的业务处理流程：先执行参数校验（check），再执行业务处理（process）。
 * 所有具体的业务操作（如创建、更新、删除、查询等）均继承此类，
 * 并实现 check 和 process 两个模板方法。
 * </p>
 * <p>调用方式：通过 {@link #execute(Object)} 方法统一触发业务流程。</p>
 *
 * @param <Request>  请求参数类型
 * @param <Response> 响应结果类型
 * @author zhao
 */
public abstract class BaseManager<Request, Response> {

    /**
     * 参数校验
     * <p>在业务处理之前执行，用于校验请求参数的合法性、业务规则等。
     * 校验不通过时应抛出 {@link org.evolve.common.web.exception.BusinessException}。</p>
     *
     * @param request 请求参数
     */
    protected abstract void check(Request request);

    /**
     * 业务处理
     * <p>在参数校验通过后执行，包含具体的业务逻辑（如数据库操作、状态变更等）。</p>
     *
     * @param request 请求参数
     * @return 业务处理结果
     */
    protected abstract Response process(Request request);

    /**
     * 执行业务流程
     * <p>统一入口，按顺序执行 check → process。</p>
     *
     * @param request 请求参数
     * @return 业务处理结果
     */
    public Response execute(Request request) {
        check(request);
        return process(request);
    }
}