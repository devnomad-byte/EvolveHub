package org.evolve.auth.service;

import jakarta.annotation.Resource;
import org.evolve.auth.request.DeptUpdateRequest;
import org.evolve.auth.response.DeptResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.model.DeptEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 更新部门业务处理器
 *
 * @author zhao
 */
@Service
public class DeptUpdateManager extends BaseManager<DeptUpdateRequest, DeptResponse> {

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(DeptUpdateRequest request) {
        // 检查部门是否存在
        DeptEntity dept = deptInfra.getById(request.id());
        if (dept == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门不存在");
        }

        // 如果指定了上级部门，检查上级部门是否存在
        if (request.parentId() != null && request.parentId() != 0) {
            DeptEntity parentDept = deptInfra.getById(request.parentId());
            if (parentDept == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "上级部门不存在");
            }

            // 不能将部门的上级部门设置为自己
            if (request.parentId().equals(request.id())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能将部门设置为自己的上级部门");
            }

            // 检查是否设置为自己的子部门（递归检查循环引用）
            if (isChildDepartment(request.parentId(), request.id())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能将部门的上级部门设置为自己的子部门");
            }
        }
    }

    /**
     * 递归检查是否是子部门
     *
     * @param potentialParentId 潜在的父部门 ID
     * @param currentDeptId     当前部门 ID
     * @return 是否是子部门
     */
    private boolean isChildDepartment(Long potentialParentId, Long currentDeptId) {
        // 查询 potentialParentId 的所有子部门
        List<DeptEntity> childDepts = deptInfra.lambdaQuery()
                .eq(DeptEntity::getParentId, potentialParentId)
                .list();

        for (DeptEntity child : childDepts) {
            // 如果子部门就是当前部门，返回 true
            if (child.getId().equals(currentDeptId)) {
                return true;
            }

            // 递归检查子部门的子部门
            if (isChildDepartment(child.getId(), currentDeptId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected DeptResponse process(DeptUpdateRequest request) {
        // 查询部门
        DeptEntity dept = deptInfra.getById(request.id());

        // 更新部门信息
        dept.setDeptName(request.deptName());
        if (request.parentId() != null) {
            dept.setParentId(request.parentId());
        }
        if (request.sort() != null) {
            dept.setSort(request.sort());
        }
        if (request.status() != null) {
            dept.setStatus(request.status());
        }
        deptInfra.updateDept(dept);

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
