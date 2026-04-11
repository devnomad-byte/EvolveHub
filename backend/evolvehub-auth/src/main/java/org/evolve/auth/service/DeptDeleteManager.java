package org.evolve.auth.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.infra.PermissionsInfra;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.model.DeptEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 删除部门业务处理器
 *
 * @author zhao
 */
@Service
public class DeptDeleteManager extends BaseManager<Long, Void> {

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private PermissionsInfra permissionsInfra;

    @Override
    protected void check(Long deptId) {
        // 检查部门是否存在
        DeptEntity dept = deptInfra.getById(deptId);
        if (dept == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门不存在");
        }

        // 检查是否有用户
        long userCount = usersInfra.countByDeptId(deptId);
        if (userCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该部门下还有 " + userCount + " 个用户，无法删除");
        }

        // 检查是否有子部门
        List<DeptEntity> childDepts = deptInfra.lambdaQuery()
                .eq(DeptEntity::getParentId, deptId)
                .list();
        if (!childDepts.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该部门下还有 " + childDepts.size() + " 个子部门，无法删除");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected Void process(Long deptId) {
        // 删除部门（软删除）
        deptInfra.removeById(deptId);

        return null;
    }
}
