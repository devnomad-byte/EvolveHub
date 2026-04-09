package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.PermissionsInfra;
import org.evolve.auth.user.model.PermissionsEntity;
import org.evolve.auth.user.request.UpdatePermissionRequest;
import org.evolve.auth.user.response.UpdatePermissionResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 更新权限业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>权限必须存在</li>
 *     <li>permCode 若修改，需保证全局唯一（排除自身）</li>
 *     <li>permType 若修改，只允许 MENU / BUTTON / API</li>
 *     <li>parentId 若修改，父权限必须存在（0 表示顶级）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class UpdatePermissionManager extends BaseManager<UpdatePermissionRequest, UpdatePermissionResponse> {

    /** 合法的权限类型集合 */
    private static final Set<String> VALID_PERM_TYPES = Set.of("MENU", "BUTTON", "API");

    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(UpdatePermissionRequest request) {
        // 权限必须存在
        if (permissionsInfra.getPermissionById(request.id()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "权限不存在");
        }
        // permCode 排他唯一性校验
        if (request.permCode() != null) {
            PermissionsEntity byCode = permissionsInfra.getByPermCode(request.permCode());
            if (byCode != null && !byCode.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "权限编码已被其他权限使用");
            }
        }
        // permType 合法性校验
        if (request.permType() != null && !VALID_PERM_TYPES.contains(request.permType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "权限类型只允许 MENU / BUTTON / API");
        }
        // 父权限存在性校验
        if (request.parentId() != null && request.parentId() != 0
                && permissionsInfra.getPermissionById(request.parentId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "父权限不存在");
        }
    }

    @Override
    protected UpdatePermissionResponse process(UpdatePermissionRequest request) {
        PermissionsEntity entity = new PermissionsEntity();
        entity.setId(request.id());
        entity.setParentId(request.parentId());
        entity.setPermName(request.permName());
        entity.setPermCode(request.permCode());
        entity.setPermType(request.permType());
        entity.setPath(request.path());
        entity.setIcon(request.icon());
        entity.setSort(request.sort());
        entity.setStatus(request.status());
        permissionsInfra.updatePermission(entity);
        return new UpdatePermissionResponse();
    }
}
