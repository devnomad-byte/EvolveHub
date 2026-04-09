package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.DeptInfra;
import org.evolve.auth.user.infra.UsersInfra;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除部门业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>部门必须存在</li>
 *     <li>存在子部门时不允许删除（需先删除子部门）</li>
 *     <li>部门下有用户时不允许删除（需先转移用户到其他部门）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class DeleteDeptManager extends BaseManager<Long, Void> {

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(Long id) {
        // 部门必须存在
        if (deptInfra.getDeptById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "部门不存在");
        }
        // 存在子部门时不允许删除
        if (deptInfra.existsChildDept(id)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该部门下存在子部门，请先删除子部门");
        }
        // 部门下有用户时不允许删除
        if (usersInfra.countByDeptId(id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该部门下仍有用户，请先将用户转移到其他部门");
        }
    }

    @Override
    protected Void process(Long id) {
        deptInfra.deleteDept(id);
        return null;
    }
}
