package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.PermissionsInfra;
import org.evolve.common.model.PermissionsEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 查询单个权限业务处理器
 * <p>
 * 根据权限ID查询权限详情，权限不存在时抛出业务异常。
 * </p>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class GetPermissionManager extends BaseManager<Long, PermissionsEntity> {

    /** 权限数据访问层 */
    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(Long id) {
        if (id == null) throw new BusinessException(ResultCode.BAD_REQUEST, "权限ID不能为空");
    }

    @Override
    protected PermissionsEntity process(Long id) {
        PermissionsEntity p = permissionsInfra.getPermissionById(id);
        if (p == null) throw new BusinessException(ResultCode.DATA_NOT_EXIST, "权限不存在");
        return p;
    }
}
