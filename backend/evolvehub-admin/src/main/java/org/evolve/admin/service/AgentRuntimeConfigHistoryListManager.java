package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.admin.response.AgentRuntimeConfigHistoryResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.page.PageResponse;
import org.evolve.domain.resource.infra.AgentRuntimeConfigHistoryInfra;
import org.evolve.domain.resource.model.AgentRuntimeConfigHistoryEntity;
import org.springframework.stereotype.Service;

/**
 * 获取Agent运行时配置变更历史
 */
@Service
public class AgentRuntimeConfigHistoryListManager extends BaseManager<AgentRuntimeConfigHistoryListManager.Request, PageResponse<AgentRuntimeConfigHistoryResponse>> {

    public record Request(String configKey, int pageNum, int pageSize) {}

    @Resource
    private AgentRuntimeConfigHistoryInfra agentRuntimeConfigHistoryInfra;

    @Override
    protected void check(Request request) {
        if (request.pageNum() < 1) {
            throw new org.evolve.common.web.exception.BusinessException(
                    org.evolve.common.web.response.ResultCode.BAD_REQUEST, "页码必须大于0");
        }
        if (request.pageSize() < 1 || request.pageSize() > 100) {
            throw new org.evolve.common.web.exception.BusinessException(
                    org.evolve.common.web.response.ResultCode.BAD_REQUEST, "每页数量必须在1-100之间");
        }
    }

    @Override
    protected PageResponse<AgentRuntimeConfigHistoryResponse> process(Request request) {
        Page<AgentRuntimeConfigHistoryEntity> page = agentRuntimeConfigHistoryInfra.pageByConfigKey(
                request.configKey(), request.pageNum(), request.pageSize());

        return new PageResponse<>(
                page.getRecords().stream()
                        .map(AgentRuntimeConfigHistoryResponse::from)
                        .toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }
}
