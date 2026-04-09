package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.PermissionsInfra;
import org.evolve.auth.user.model.PermissionsEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 查询权限详情业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class GetPermissionManager extends BaseManager<Long, PermissionsEntity> {

    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "权限ID不能为空");
        }
    }

    @Override
    protected PermissionsEntity process(Long id) {
        PermissionsEntity permission = permissionsInfra.getPermissionById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "权限不存在");
        }
        return permission;
    }
}
