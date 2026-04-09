package org.evolve.auth.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.DeptInfra;
import org.evolve.auth.user.model.DeptEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 部门分页列表业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class ListDeptManager extends BaseManager<PageRequest, PageResponse<DeptEntity>> {

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<DeptEntity> process(PageRequest request) {
        Page<DeptEntity> page = deptInfra.listPage(request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
