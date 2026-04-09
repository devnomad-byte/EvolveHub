package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.RolesInfra;
import org.evolve.auth.user.model.RolesEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 查询角色详情业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class GetRoleManager extends BaseManager<Long, RolesEntity> {

    @Resource
    private RolesInfra rolesInfra;

    @Override
    protected void check(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色ID不能为空");
        }
    }

    @Override
    protected RolesEntity process(Long id) {
        RolesEntity role = rolesInfra.getRoleById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
        }
        return role;
    }
}
