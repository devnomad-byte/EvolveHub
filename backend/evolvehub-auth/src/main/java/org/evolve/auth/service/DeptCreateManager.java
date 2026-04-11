package org.evolve.auth.service;

import jakarta.annotation.Resource;
import org.evolve.auth.request.DeptCreateRequest;
import org.evolve.auth.response.DeptResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.model.DeptEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建部门业务处理器
 *
 * @author zhao
 */
@Service
public class DeptCreateManager extends BaseManager<DeptCreateRequest, DeptResponse> {

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(DeptCreateRequest request) {
        // 如果指定了上级部门，检查上级部门是否存在
        if (request.parentId() != null && request.parentId() != 0) {
            DeptEntity parentDept = deptInfra.getById(request.parentId());
            if (parentDept == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "上级部门不存在");
            }
        }
    }

    @Override
    protected DeptResponse process(DeptCreateRequest request) {
        // 创建部门
        DeptEntity dept = new DeptEntity();
        dept.setDeptName(request.deptName());
        dept.setParentId(request.parentId() != null ? request.parentId() : 0L);
        dept.setSort(request.sort() != null ? request.sort() : 0);
        dept.setStatus(request.status() != null ? request.status() : 1);
        deptInfra.createDept(dept);

        // 返回部门信息
        return new DeptResponse(
                dept.getId(),
                dept.getDeptName(),
                dept.getParentId(),
                dept.getSort(),
                dept.getStatus(),
                dept.getCreateTime(),
                dept.getUpdateTime(),
                null
        );
    }
}
