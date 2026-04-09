package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.PermissionsInfra;
import org.evolve.auth.user.model.PermissionsEntity;
import org.evolve.auth.user.request.CreatePermissionRequest;
import org.evolve.auth.user.response.CreatePermissionResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 创建权限业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>权限编码（permCode）全局唯一</li>
 *     <li>权限类型（permType）只允许 MENU / BUTTON / API</li>
 *     <li>parentId 非 0 时父权限必须存在</li>
 *     <li>新权限默认状态为正常（status=1）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class CreatePermissionManager extends BaseManager<CreatePermissionRequest, CreatePermissionResponse> {

    /** 合法的权限类型集合 */
    private static final Set<String> VALID_PERM_TYPES = Set.of("MENU", "BUTTON", "API");

    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(CreatePermissionRequest request) {
        // 权限编码不能重复
        if (permissionsInfra.getByPermCode(request.permCode()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "权限编码已存在");
        }
        // 权限类型合法性校验
        if (!VALID_PERM_TYPES.contains(request.permType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "权限类型只允许 MENU / BUTTON / API");
        }
        // 父权限存在性校验（parentId = 0 表示顶级，无需校验）
        if (request.parentId() != 0 && permissionsInfra.getPermissionById(request.parentId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "父权限不存在");
        }
    }

    @Override
    protected CreatePermissionResponse process(CreatePermissionRequest request) {
        PermissionsEntity entity = new PermissionsEntity();
        entity.setParentId(request.parentId());
        entity.setPermName(request.permName());
        entity.setPermCode(request.permCode());
        entity.setPermType(request.permType());
        entity.setPath(request.path());
        entity.setIcon(request.icon());
        entity.setSort(request.sort() != null ? request.sort() : 0);
        entity.setStatus(1);
        permissionsInfra.createPermission(entity);
        return new CreatePermissionResponse(entity.getId());
    }
}
