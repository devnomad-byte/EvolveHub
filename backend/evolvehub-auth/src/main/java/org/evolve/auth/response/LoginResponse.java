package org.evolve.auth.response;

import java.util.List;

/**
 * 登录响应
 *
 * @param token       Sa-Token 令牌
 * @param userId      用户 ID
 * @param username    用户名
 * @param nickname    昵称
 * @param roles       角色编码列表
 * @param permissions 权限编码列表
 */
public record LoginResponse(
        String token,
        Long userId,
        String username,
        String nickname,
        List<String> roles,
        List<String> permissions) {
}
