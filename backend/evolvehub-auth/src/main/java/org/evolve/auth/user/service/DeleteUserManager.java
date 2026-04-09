package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.UserRolesInfra;
import org.evolve.auth.user.infra.UsersInfra;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除用户业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>用户必须存在</li>
 *     <li>删除用户时级联清理 t_user_role 关联记录</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class DeleteUserManager extends BaseManager<Long, Void> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Override
    protected void check(Long id) {
        // 用户必须存在
        if (usersInfra.getUserById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }
    }

    @Override
    protected Void process(Long id) {
        // 先清理用户的角色关联
        userRolesInfra.removeByUserId(id);
        // 再删除用户
        usersInfra.deleteUser(id);
        return null;
    }
}
