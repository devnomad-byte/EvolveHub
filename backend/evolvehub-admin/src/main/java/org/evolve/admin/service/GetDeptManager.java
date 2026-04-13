package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.model.DeptEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 查询单个部门业务处理器
 * <p>
 * 根据部门ID查询部门详情，部门不存在时抛出业务异常。
 * </p>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class GetDeptManager extends BaseManager<Long, DeptEntity> {

    /** 部门数据访问层 */
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
