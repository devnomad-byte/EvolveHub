package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.request.TokenUsageQueryRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ChatTokenUsageInfra;
import org.evolve.domain.resource.model.ChatTokenUsageEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenUsageManager extends BaseManager<TokenUsageQueryRequest, List<ChatTokenUsageEntity>> {

    @Resource
    private ChatTokenUsageInfra chatTokenUsageInfra;

    @Override
    protected void check(TokenUsageQueryRequest request) {}

    @Override
    protected List<ChatTokenUsageEntity> process(TokenUsageQueryRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return chatTokenUsageInfra.listByUserAndDateRange(userId, request.startDate(), request.endDate());
    }
}
