package org.evolve.admin.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应
 *
 * @author zhao
 */
public record UserResponse(

        /**
         * 用户 ID
         */
        Long id,

        /**
         * 用户名
         */
        String username,

        /**
         * 昵称
         */
        String nickname,

        /**
         * 邮箱
         */
        String email,

        /**
         * 手机号
         */
        String phone,

        /**
         * 头像 URL
         */
        String avatar,

        /**
         * 所属部门 ID
         */
        Long deptId,

        /**
         * 部门名称
         */
        String deptName,

        /**
         * 角色列表
         */
        List<RoleInfo> roles,

        /**
         * 状态（0-禁用 1-正常）
         */
        Integer status,

        /**
         * 创建时间
         */
        LocalDateTime createTime,

        /**
         * 更新时间
         */
        LocalDateTime updateTime
) {

    /**
     * 角色信息
     */
    public record RoleInfo(
            Long id,
            String roleName,
            String roleCode
    ) {
    }
}
