package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.DeptInfra;
import org.evolve.auth.user.model.DeptEntity;
import org.evolve.auth.user.request.UpdateDeptRequest;
import org.evolve.auth.user.response.UpdateDeptResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新部门业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>部门必须存在</li>
 *     <li>parentId 不能设为自身（防止循环引用）</li>
 *     <li>parentId 非 0 时父部门必须存在</li>
 *     <li>同级部门名称唯一（排除自身）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class UpdateDeptManager extends BaseManager<UpdateDeptRequest, UpdateDeptResponse> {

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(UpdateDeptRequest request) {
        // 部门必须存在
        DeptEntity existing = deptInfra.getDeptById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门不存在");
        }
        // 不能把自己设为自己的父部门
        if (request.parentId() != null && request.parentId().equals(request.id())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "父部门不能是自身");
        }
        // 父部门存在性校验
        if (request.parentId() != null && request.parentId() != 0
                && deptInfra.getDeptById(request.parentId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "父部门不存在");
        }
        // 同级部门名称排他唯一性校验
        if (request.deptName() != null) {
            Long parentId = request.parentId() != null ? request.parentId() : existing.getParentId();
            DeptEntity byName = deptInfra.getByParentIdAndName(parentId, request.deptName());
            if (byName != null && !byName.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "同级下已存在同名部门");
            }
        }
    }

    @Override
    protected UpdateDeptResponse process(UpdateDeptRequest request) {
        DeptEntity entity = new DeptEntity();
        entity.setId(request.id());
        entity.setParentId(request.parentId());
        entity.setDeptName(request.deptName());
        entity.setSort(request.sort());
        entity.setStatus(request.status());
        deptInfra.updateDept(entity);
        return new UpdateDeptResponse();
    }
}
