package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.DeptInfra;
import org.evolve.auth.user.model.DeptEntity;
import org.evolve.auth.user.request.CreateDeptRequest;
import org.evolve.auth.user.response.CreateDeptResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建部门业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>同一父部门下部门名称唯一</li>
 *     <li>parentId 非 0 时父部门必须存在</li>
 *     <li>新部门默认状态为正常（status=1）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class CreateDeptManager extends BaseManager<CreateDeptRequest, CreateDeptResponse> {

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(CreateDeptRequest request) {
        // 父部门存在性校验（parentId = 0 表示顶级，无需校验）
        if (request.parentId() != 0 && deptInfra.getDeptById(request.parentId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "父部门不存在");
        }
        // 同级部门名称不能重复
        if (deptInfra.getByParentIdAndName(request.parentId(), request.deptName()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "同级下已存在同名部门");
        }
    }

    @Override
    protected CreateDeptResponse process(CreateDeptRequest request) {
        DeptEntity entity = new DeptEntity();
        entity.setParentId(request.parentId());
        entity.setDeptName(request.deptName());
        entity.setSort(request.sort() != null ? request.sort() : 0);
        entity.setStatus(1);
        deptInfra.createDept(entity);
        return new CreateDeptResponse(entity.getId());
    }
}
