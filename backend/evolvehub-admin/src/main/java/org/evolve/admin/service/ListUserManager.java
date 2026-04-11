package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.model.UsersEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 用户分页列表业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class ListUserManager extends BaseManager<PageRequest, PageResponse<UsersEntity>> {

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<UsersEntity> process(PageRequest request) {
        Page<UsersEntity> page = usersInfra.listPage(request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
