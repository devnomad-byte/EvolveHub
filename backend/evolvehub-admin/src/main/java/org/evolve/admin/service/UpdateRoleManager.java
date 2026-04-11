package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.infra.RoleDataScopeInfra;
import org.evolve.common.infra.RolesInfra;
import org.evolve.common.model.RolesEntity;
import org.evolve.admin.request.UpdateRoleRequest;
import org.evolve.admin.response.UpdateRoleResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新角色业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>角色必须存在</li>
 *     <li>角色名称若修改，需保证全局唯一（排除自身）</li>
 *     <li>角色编码若修改，需保证全局唯一（排除自身）</li>
 *     <li>dataScope 若修改，必须在 1~5 范围内</li>
 *     <li>dataScope 从 5（自定义）改为其他值时，自动清理 t_role_data_scope 关联数据</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class UpdateRoleManager extends BaseManager<UpdateRoleRequest, UpdateRoleResponse> {

    @Resource
    private RolesInfra rolesInfra;

    @Resource
    private RoleDataScopeInfra roleDataScopeInfra;

    @Override
    protected void check(UpdateRoleRequest request) {
        RolesEntity existing = rolesInfra.getRoleById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
        }
        if (request.roleName() != null) {
            RolesEntity byName = rolesInfra.getByRoleName(request.roleName());
            if (byName != null && !byName.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "角色名称已被其他角色使用");
            }
        }
        if (request.roleCode() != null) {
            RolesEntity byCode = rolesInfra.getByRoleCode(request.roleCode());
            if (byCode != null && !byCode.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "角色编码已被其他角色使用");
            }
        }
        if (request.dataScope() != null && (request.dataScope() < 1 || request.dataScope() > 5)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "数据权限范围必须为 1~5");
        }
    }

    @Override
    protected UpdateRoleResponse process(UpdateRoleRequest request) {
        RolesEntity existing = rolesInfra.getRoleById(request.id());
        if (existing.getDataScope() == 5 && request.dataScope() != null && request.dataScope() != 5) {
            roleDataScopeInfra.removeByRoleId(request.id());
        }

        RolesEntity entity = new RolesEntity();
        entity.setId(request.id());
        entity.setRoleName(request.roleName());
        entity.setRoleCode(request.roleCode());
        entity.setDataScope(request.dataScope());
        entity.setSort(request.sort());
        entity.setStatus(request.status());
        entity.setRemark(request.remark());
        rolesInfra.updateRole(entity);
        return new UpdateRoleResponse();
    }
}
