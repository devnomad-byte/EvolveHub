package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.evolve.auth.user.infra.DeptInfra;
import org.evolve.auth.user.model.DeptEntity;
import org.evolve.auth.user.request.CreateDeptRequest;
import org.evolve.auth.user.response.CreateDeptResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateDeptManager extends BaseManager<CreateDeptRequest, CreateDeptResponse> {

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(CreateDeptRequest createDeptRequest) {

        // 部门名称不能重复
        if (deptInfra.getDeptByName(createDeptRequest.deptName()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST);
        }
        // 父部门是否存在
        if (deptInfra.getDeptByParentId(createDeptRequest.parentId()) != null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
    }

    @Override
    protected CreateDeptResponse process(CreateDeptRequest createDeptRequest) {
        DeptEntity deptEntity = new DeptEntity();
        deptEntity.setDeptName(createDeptRequest.deptName());
        deptEntity.setParentId(createDeptRequest.parentId());
        deptInfra.createDept(deptEntity);
        return new CreateDeptResponse();
    }
}
