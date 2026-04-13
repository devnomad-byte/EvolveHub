package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.infra.RoleDataScopeInfra;
import org.evolve.domain.rbac.infra.RolesInfra;
import org.evolve.domain.rbac.model.RoleDataScopeEntity;
import org.evolve.domain.rbac.model.RolesEntity;
import org.evolve.admin.request.AssignRoleDataScopeRequest;
import org.evolve.admin.response.AssignRoleDataScopeResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 设置角色自定义数据范围业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>角色必须存在</li>
 *     <li>角色 dataScope 必须为 5（自定义），否则不允许设置部门列表</li>
 *     <li>所有指定的部门 ID 必须存在</li>
 *     <li>执行时全量替换（先清除旧数据，再写入新数据）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class AssignRoleDataScopeManager extends BaseManager<AssignRoleDataScopeRequest, AssignRoleDataScopeResponse> {

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private RoleDataScopeInfra roleDataScopeInfra;

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(AssignRoleDataScopeRequest request) {
        RolesEntity role = rolesInfra.getRoleById(request.roleId());
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
        }
        if (role.getDataScope() != 5) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅数据权限为「自定义」的角色才允许设置部门列表");
        }
        for (Long deptId : request.deptIds()) {
            if (deptInfra.getDeptById(deptId) == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门ID=" + deptId + " 不存在");
            }
        }
    }

    @Override
    protected AssignRoleDataScopeResponse process(AssignRoleDataScopeRequest request) {
        roleDataScopeInfra.removeByRoleId(request.roleId());
        for (Long deptId : request.deptIds()) {
            RoleDataScopeEntity entity = new RoleDataScopeEntity();
            entity.setRoleId(request.roleId());
            entity.setDeptId(deptId);
            roleDataScopeInfra.assignDataScope(entity);
        }
        return new AssignRoleDataScopeResponse();
    }
}
