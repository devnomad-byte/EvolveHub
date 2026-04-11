package org.evolve.common.datascope;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>
 * 标注在 Mapper 方法上，表示该查询需要进行数据权限过滤。
 * 拦截器会根据当前用户的 dataScope 自动在 SQL 中追加 WHERE 条件。
 * </p>
 *
 * @author zhao
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 部门表的别名（用于拼接 WHERE alias.dept_id IN (...)）
     * <p>默认为空字符串，表示不加别名前缀（直接 WHERE dept_id IN (...)）</p>
     */
    String deptAlias() default "";

    /**
     * 用户表的别名（用于拼接 WHERE alias.create_by = ...，仅 dataScope=4 时生效）
     * <p>默认为空字符串，表示不加别名前缀</p>
     */
    String userAlias() default "";
}
