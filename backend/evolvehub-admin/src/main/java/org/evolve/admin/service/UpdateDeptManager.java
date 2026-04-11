package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateDeptRequest;
import org.evolve.admin.response.UpdateDeptResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.model.DeptEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新部门业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>部门必须存在</li>
 *     <li>不允许将父部门设置为自身（循环引用防护）</li>
 *     <li>父部门必须存在（parentId=0 表示顶级）</li>
 *     <li>同级部门名称排他唯一性校验</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class UpdateDeptManager extends BaseManager<UpdateDeptRequest, UpdateDeptResponse> {

    /** 部门数据访问层 */
    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(UpdateDeptRequest request) {
        DeptEntity existing = deptInfra.getDeptById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门不存在");
        }
        if (request.parentId() != null && request.parentId().equals(request.id())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "父部门不能是自身");
        }
        if (request.parentId() != null && request.parentId() != 0
                && deptInfra.getDeptById(request.parentId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "父部门不存在");
        }
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
