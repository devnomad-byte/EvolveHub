package org.evolve.auth.response;

/**
 * 注册响应
 *
 * @param userId   新用户 ID
 * @param username 用户名
 */
public record RegisterResponse(Long userId, String username) {
}
