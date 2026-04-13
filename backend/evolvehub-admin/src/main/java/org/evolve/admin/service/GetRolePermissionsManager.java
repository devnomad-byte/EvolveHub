package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.infra.RolePermissionsInfra;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 查询角色已有权限列表业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Service
public class GetRolePermissionsManager extends BaseManager<Long, List<Long>> {

    @Resource
    private RolePermissionsInfra rolePermissionsInfra;

    @Override
    protected void check(Long roleId) {
        if (roleId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色ID不能为空");
        }
    }

    @Override
    protected List<Long> process(Long roleId) {
        return rolePermissionsInfra.listByRoleId(roleId).stream()
                .map(rp -> rp.getPermissionId())
                .toList();
    }
}
