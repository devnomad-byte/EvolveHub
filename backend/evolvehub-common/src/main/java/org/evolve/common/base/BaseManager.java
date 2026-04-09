package org.evolve.common.base;

/**
 * @author zhao
 * @version v1.0
 * @className BaseManager
 * @description 业务处理父类
 * @date 2026/4/9 16:13
 **/
public abstract class BaseManager<Request, Response> {

    protected abstract void check(Request request);

    protected abstract Response process(Request request);

    public Response execute(Request request) {
        check(request);
        return process(request);
    }
}