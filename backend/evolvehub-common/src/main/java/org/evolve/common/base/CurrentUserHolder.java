package org.evolve.common.base;

/**
 * 当前登录用户 ID 持有者（ThreadLocal）
 * <p>
 * 由各业务模块的 Web 拦截器在请求进入时设置，供 MyBatis 自动填充等通用组件使用。
 * common 模块不依赖 Sa-Token，通过此类解耦。
 * </p>
 *
 * @author zhao
 */
public final class CurrentUserHolder {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private CurrentUserHolder() {}

    /**
     * 设置当前线程的用户 ID
     *
     * @param userId 用户 ID
     */
    public static void set(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取当前线程的用户 ID
     *
     * @return 用户 ID，未设置时返回 null
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 清除当前线程的用户 ID
     */
    public static void clear() {
        USER_ID.remove();
    }
}
