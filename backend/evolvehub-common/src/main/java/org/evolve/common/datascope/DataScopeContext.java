package org.evolve.common.datascope;

import lombok.Builder;

import java.util.Set;

/**
 * 数据权限上下文
 * <p>
 * 通过 {@link DataScopeContextHolder} 在请求线程中传递，
 * MyBatis 拦截器根据此上下文自动追加 SQL 数据过滤条件。
 * </p>
 *
 * @param userId         当前登录用户 ID
 * @param deptId         当前用户所属部门 ID
 * @param scopeType      数据权限范围类型：
 *                       <ul>
 *                       <li>1 - 全部数据（不过滤）</li>
 *                       <li>2 - 本部门及子部门数据</li>
 *                       <li>3 - 仅本部门数据</li>
 *                       <li>4 - 仅本人数据</li>
 *                       <li>5 - 自定义部门数据</li>
 *                       </ul>
 * @param visibleDeptIds 可见部门 ID 集合（scopeType=2/3/5 时有值）
 *                       <p>scopeType=1 时为 null（不过滤），scopeType=4 时通过 userId 过滤</p>
 * @author zhao
 */
@Builder
public record DataScopeContext(Long userId, Long deptId, Integer scopeType, Set<Long> visibleDeptIds) {

}
