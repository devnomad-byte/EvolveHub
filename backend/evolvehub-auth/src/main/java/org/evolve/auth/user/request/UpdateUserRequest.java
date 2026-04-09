package org.evolve.auth.user.request;

import jakarta.validation.constraints.NotNull;

/**
 * 更新用户请求
 * <p>
 * 仅允许修改个人信息和归属部门，不允许修改用户名（登录账号不可变更）。
 * 传 null 的字段不会被更新（MyBatis-Plus updateById 默认策略）。
 *
 * @param id       用户 ID（必填）
 * @param nickname 昵称
 * @param email    邮箱（若传值，需保证全局唯一）
 * @param phone    手机号（若传值，需保证全局唯一）
 * @param avatar   头像 URL
 * @param deptId   所属部门 ID（若传值，部门必须存在且正常）
 * @param status   状态（0-禁用 1-正常）
 */
public record UpdateUserRequest(
        @NotNull(message = "用户ID不能为空")
        Long id,
        String nickname,
        String email,
        String phone,
        String avatar,
        Long deptId,
        Integer status) {
}
