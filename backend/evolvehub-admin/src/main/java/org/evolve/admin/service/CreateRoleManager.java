package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.domain.rbac.infra.RolesInfra;
import org.evolve.domain.rbac.model.RolesEntity;
import org.evolve.admin.request.CreateRoleRequest;
import org.evolve.admin.response.CreateRoleResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建角色业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>角色名称全局唯一</li>
 *     <li>角色编码全局唯一</li>
 *     <li>dataScope 必须在 1~5 的合法范围内</li>
 *     <li>新角色默认状态为正常（status=1）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class CreateRoleManager extends BaseManager<CreateRoleRequest, CreateRoleResponse> {

    @Resource
    private RolesInfra rolesInfra;

    @Override
    protected void check(CreateRoleRequest request) {
        if (rolesInfra.getByRoleName(request.roleName()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "角色名称已存在");
        }
        if (rolesInfra.getByRoleCode(request.roleCode()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "角色编码已存在");
        }
        if (request.dataScope() < 1 || request.dataScope() > 5) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "数据权限范围必须为 1~5");
        }
    }

    @Override
    protected CreateRoleResponse process(CreateRoleRequest request) {
        RolesEntity entity = new RolesEntity();
        entity.setRoleName(request.roleName());
        entity.setRoleCode(request.roleCode());
        entity.setDataScope(request.dataScope());
        entity.setSort(request.sort() != null ? request.sort() : 0);
        entity.setRemark(request.remark());
        entity.setStatus(1);
        rolesInfra.createRole(entity);
        return new CreateRoleResponse(entity.getId());
    }
}
