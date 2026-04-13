package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.model.DeptEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询部门列表业务处理器
 * <p>
 * 支持分页查询部门列表，无额外业务校验。
 * </p>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class ListDeptManager extends BaseManager<PageRequest, PageResponse<DeptEntity>> {

    /** 部门数据访问层 */
    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(PageRequest request) {}

    @Override
    protected PageResponse<DeptEntity> process(PageRequest request) {
        Page<DeptEntity> page = deptInfra.listPage(request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
