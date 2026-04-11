package org.evolve.auth.response;

import java.util.List;

/**
 * 当前登录用户信息响应
 *
 * @author zhao
 */
public record CurrentUserResponse(

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
         * 头像 URL
         */
        String avatar,

        /**
         * 所属部门 ID
         */
        Long deptId,

        /**
         * 所属部门名称
         */
        String deptName,

        /**
         * 角色码列表
         */
        List<String> roles,

        /**
         * 权限码列表
         */
        List<String> permissions
) {
}
