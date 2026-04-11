package org.evolve.common.datascope;

/**
 * 数据权限上下文持有者（ThreadLocal）
 * <p>
 * 在每次请求进入时由 Web 过滤器/拦截器设置，
 * MyBatis 拦截器在执行 SQL 时读取。请求结束后必须清除，防止内存泄漏。
 * </p>
 *
 * @author zhao
 */
public final class DataScopeContextHolder {

    private static final ThreadLocal<DataScopeContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> FILTER_ENABLED = ThreadLocal.withInitial(() -> false);

    private DataScopeContextHolder() {}

    /**
     * 设置当前线程的数据权限上下文
     *
     * @param context 数据权限上下文
     */
    public static void set(DataScopeContext context) {
        CONTEXT.set(context);
    }

    /**
     * 获取当前线程的数据权限上下文
     *
     * @return 数据权限上下文，未设置时返回 null
     */
    public static DataScopeContext get() {
        return CONTEXT.get();
    }

    /**
     * 清除当前线程的数据权限上下文（必须在请求结束时调用）
     */
    public static void clear() {
        CONTEXT.remove();
        FILTER_ENABLED.remove();
    }

    /**
     * 启用数据权限过滤（在需要过滤的查询前调用）
     */
    public static void enableFilter() {
        FILTER_ENABLED.set(true);
    }

    /**
     * 禁用数据权限过滤（在查询完成后调用）
     */
    public static void disableFilter() {
        FILTER_ENABLED.set(false);
    }

    /**
     * 当前是否启用了数据权限过滤
     */
    public static boolean isFilterEnabled() {
        return Boolean.TRUE.equals(FILTER_ENABLED.get());
    }
}
