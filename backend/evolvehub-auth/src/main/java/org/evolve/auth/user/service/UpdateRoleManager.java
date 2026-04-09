package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.RoleDataScopeInfra;
import org.evolve.auth.user.infra.RolesInfra;
import org.evolve.auth.user.model.RolesEntity;
import org.evolve.auth.user.request.UpdateRoleRequest;
import org.evolve.auth.user.response.UpdateRoleResponse;
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
        // 角色必须存在
        RolesEntity existing = rolesInfra.getRoleById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "角色不存在");
        }
        // 角色名称排他唯一性校验
        if (request.roleName() != null) {
            RolesEntity byName = rolesInfra.getByRoleName(request.roleName());
            if (byName != null && !byName.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "角色名称已被其他角色使用");
            }
        }
        // 角色编码排他唯一性校验
        if (request.roleCode() != null) {
            RolesEntity byCode = rolesInfra.getByRoleCode(request.roleCode());
            if (byCode != null && !byCode.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "角色编码已被其他角色使用");
            }
        }
        // dataScope 合法范围校验
        if (request.dataScope() != null && (request.dataScope() < 1 || request.dataScope() > 5)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "数据权限范围必须为 1~5");
        }
    }

    @Override
    protected UpdateRoleResponse process(UpdateRoleRequest request) {
        // 如果 dataScope 从 5 改为非 5，清理自定义部门列表
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
