package org.evolve.admin.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 更新用户请求
 *
 * @param id       用户ID（必填）
 * @param nickname 昵称
 * @param email    邮箱（全局唯一）
 * @param phone    手机号（全局唯一）
 * @param avatar   头像地址
 * @param deptId   所属部门ID（部门必须存在）
 * @param roleIds  角色ID列表（传入则替换所有角色）
 * @param status   状态（1=正常 0=停用）
 */
public record UpdateUserRequest(
        @NotNull(message = "用户ID不能为空") Long id,
        String nickname,
        String email,
        String phone,
        String avatar,
        Long deptId,
        List<Long> roleIds,
        Integer status
) {
}
