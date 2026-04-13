package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询当前用户的用户级模型配置列表
 *
 * @author zhao
 */
@Service
public class ListUserModelConfigManager extends BaseManager<PageRequest, PageResponse<ModelConfigEntity>> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<ModelConfigEntity> process(PageRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Page<ModelConfigEntity> page = modelConfigInfra.listPageByOwnerId(currentUserId, request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
