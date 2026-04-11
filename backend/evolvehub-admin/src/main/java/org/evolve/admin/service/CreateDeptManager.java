package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateDeptRequest;
import org.evolve.admin.response.CreateDeptResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.model.DeptEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建部门业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>父部门必须存在（parentId=0 表示顶级部门）</li>
 *     <li>同级部门名称不允许重复</li>
 *     <li>新部门默认状态为正常（status=1）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class CreateDeptManager extends BaseManager<CreateDeptRequest, CreateDeptResponse> {

    /** 部门数据访问层 */
    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(CreateDeptRequest request) {
        if (request.parentId() != 0 && deptInfra.getDeptById(request.parentId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "父部门不存在");
        }
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
