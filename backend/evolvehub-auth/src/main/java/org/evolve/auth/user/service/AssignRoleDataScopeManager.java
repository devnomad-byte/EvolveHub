package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.DeptInfra;
import org.evolve.auth.user.infra.RoleDataScopeInfra;
import org.evolve.auth.user.infra.RolesInfra;
import org.evolve.auth.user.model.RoleDataScopeEntity;
import org.evolve.auth.user.model.RolesEntity;
import org.evolve.auth.user.request.AssignRoleDataScopeRequest;
import org.evolve.auth.user.response.AssignRoleDataScopeResponse;
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
        // 角色必须存在
        RolesEntity role = rolesInfra.getRoleById(request.roleId());
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
        }
        // 只有 dataScope=5（自定义）才允许设置部门列表
        if (role.getDataScope() != 5) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅数据权限为「自定义」的角色才允许设置部门列表");
        }
        // 校验所有部门 ID 是否存在
        for (Long deptId : request.deptIds()) {
            if (deptInfra.getDeptById(deptId) == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门ID=" + deptId + " 不存在");
            }
        }
    }

    @Override
    protected AssignRoleDataScopeResponse process(AssignRoleDataScopeRequest request) {
        // 先清除旧的自定义数据范围
        roleDataScopeInfra.removeByRoleId(request.roleId());
        // 批量写入新的
        for (Long deptId : request.deptIds()) {
            RoleDataScopeEntity entity = new RoleDataScopeEntity();
            entity.setRoleId(request.roleId());
            entity.setDeptId(deptId);
            roleDataScopeInfra.assignDataScope(entity);
        }
        return new AssignRoleDataScopeResponse();
    }
}
