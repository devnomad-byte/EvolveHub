package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.domain.resource.infra.EnvVarInfra;
import org.evolve.domain.resource.model.EnvVarEntity;
import org.springframework.stereotype.Service;

@Service
public class ListEnvVarManager extends BaseManager<ListEnvVarManager.Request, PageResponse> {

    public record Request(String group, int pageNum, int pageSize) {}

    @Resource
    private EnvVarInfra envVarInfra;

    @Override
    protected void check(Request request) {}

    @Override
    protected PageResponse process(Request request) {
        Page<EnvVarEntity> page = envVarInfra.listPage(request.group(), request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
