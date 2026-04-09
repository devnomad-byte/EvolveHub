package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.DeptInfra;
import org.evolve.auth.user.model.DeptEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 查询部门详情业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class GetDeptManager extends BaseManager<Long, DeptEntity> {

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门ID不能为空");
        }
    }

    @Override
    protected DeptEntity process(Long id) {
        DeptEntity dept = deptInfra.getDeptById(id);
        if (dept == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门不存在");
        }
        return dept;
    }
}
