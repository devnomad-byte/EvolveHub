package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.bean.entity.ChatTokenUsageEntity;
import org.evolve.aiplatform.infra.ChatTokenUsageInfra;
import org.evolve.aiplatform.request.TokenUsageQueryRequest;
import org.evolve.common.base.BaseManager;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Token 消费统计查询
 * <p>
 * 按日期范围查询当前用户的 token 消费日报记录。
 * </p>
 *
 * @author zhao
 */
@Service
public class TokenUsageManager extends BaseManager<TokenUsageQueryRequest, List<ChatTokenUsageEntity>> {

    @Resource
    private ChatTokenUsageInfra chatTokenUsageInfra;

    @Override
    protected void check(TokenUsageQueryRequest request) {
    }

    @Override
    protected List<ChatTokenUsageEntity> process(TokenUsageQueryRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        return chatTokenUsageInfra.listByUserAndDateRange(
                currentUserId, request.startDate(), request.endDate());
    }
}
