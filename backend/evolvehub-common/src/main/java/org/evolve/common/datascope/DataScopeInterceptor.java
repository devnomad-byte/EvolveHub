package org.evolve.common.datascope;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器（MyBatis-Plus InnerInterceptor）
 * <p>
 * 拦截 SELECT 语句，根据 {@link DataScopeContextHolder} 中的上下文信息，
 * 自动在 SQL 末尾追加数据权限过滤条件。
 * </p>
 * <p>
 * 仅当 {@link DataScopeContextHolder} 中存在上下文且 scopeType != 1 时生效。
 * 要跳过数据权限过滤的查询，在调用前临时清除 ThreadLocal 即可。
 * </p>
 *
 * @author zhao
 */
@Slf4j
public class DataScopeInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {

        // 只拦截 SELECT
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return;
        }

        // 检查是否显式启用了数据权限过滤
        if (!DataScopeContextHolder.isFilterEnabled()) {
            return;
        }

        // 获取当前线程的数据权限上下文
        DataScopeContext context = DataScopeContextHolder.get();
        if (context == null || context.getScopeType() == null || context.getScopeType() == 1) {
            // 无上下文或全部数据权限，不做过滤
            return;
        }

        // 构建数据权限 SQL 条件（默认无别名）
        String condition = buildCondition(context, "", "");
        if (condition == null || condition.isEmpty()) {
            return;
        }

        // 追加到原始 SQL（安全处理 WHERE 子句）
        String originalSql = boundSql.getSql();
        String upperSql = originalSql.toUpperCase();
        String newSql;
        if (upperSql.contains("WHERE")) {
            newSql = originalSql + " AND " + condition;
        } else {
            newSql = originalSql + " WHERE " + condition;
        }

        // 通过反射修改 BoundSql 中的 SQL
        try {
            java.lang.reflect.Field sqlField = BoundSql.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            sqlField.set(boundSql, newSql);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("数据权限拦截器修改SQL失败", e);
        }

        log.debug("数据权限过滤 scopeType={}, SQL: {}", context.getScopeType(), newSql);
    }

    /**
     * 根据 dataScope 类型构建 SQL 过滤条件
     *
     * @param context   数据权限上下文
     * @param deptAlias 部门列别名
     * @param userAlias 用户列别名
     * @return SQL 条件片段，如 "dept_id IN (1,2,3)" 或 "create_by = 100"
     */
    private String buildCondition(DataScopeContext context, String deptAlias, String userAlias) {
        String deptColumn = buildColumn(deptAlias, "dept_id");
        String userColumn = buildColumn(userAlias, "create_by");

        return switch (context.getScopeType()) {
            case 2, 3, 5 -> {
                // 本部门及子部门 / 仅本部门 / 自定义部门
                Set<Long> deptIds = context.getVisibleDeptIds();
                if (deptIds == null || deptIds.isEmpty()) {
                    // 无可见部门，返回不可能满足的条件（相当于无权限）
                    yield "1 = 0";
                }
                String ids = deptIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                yield deptColumn + " IN (" + ids + ")";
            }
            case 4 -> {
                // 仅本人数据
                yield userColumn + " = " + context.getUserId();
            }
            default -> null;
        };
    }

    /**
     * 拼接列名（支持表别名）
     */
    private String buildColumn(String alias, String column) {
        if (alias == null || alias.isEmpty()) {
            return column;
        }
        return alias + "." + column;
    }
}
