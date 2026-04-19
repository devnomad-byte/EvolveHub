package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateAgentRuntimeConfigRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.AgentRuntimeConfigHistoryInfra;
import org.evolve.domain.resource.infra.AgentRuntimeConfigInfra;
import org.evolve.domain.resource.model.AgentRuntimeConfigEntity;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 更新Agent运行时配置
 */
@Service
public class AgentRuntimeConfigUpdateManager extends BaseManager<UpdateAgentRuntimeConfigRequest, Void> {

    @Resource
    private AgentRuntimeConfigInfra agentRuntimeConfigInfra;

    @Resource
    private AgentRuntimeConfigHistoryInfra agentRuntimeConfigHistoryInfra;

    @Resource
    private UsersInfra usersInfra;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 允许的配置键列表 */
    private static final List<String> ALLOWED_CONFIG_KEYS = List.of(
            "llm_retry",
            "context_compact",
            "tool_result_compact",
            "memory_summary",
            "embedding",
            "runtime_basic"
    );

    @Override
    protected void check(UpdateAgentRuntimeConfigRequest request) {
        // 校验配置键是否合法
        if (!ALLOWED_CONFIG_KEYS.contains(request.configKey())) {
            throw new org.evolve.common.web.exception.BusinessException(
                    org.evolve.common.web.response.ResultCode.BAD_REQUEST, "非法的配置键: " + request.configKey());
        }

        // 校验JSON格式是否有效
        try {
            objectMapper.readTree(request.configValue());
        } catch (JsonProcessingException e) {
            throw new org.evolve.common.web.exception.BusinessException(
                    org.evolve.common.web.response.ResultCode.BAD_REQUEST, "配置值必须是有效的JSON格式");
        }
    }

    @Override
    protected Void process(UpdateAgentRuntimeConfigRequest request) {
        // 获取当前登录用户信息
        Long operatorId = StpUtil.getLoginIdAsLong();
        UsersEntity operator = usersInfra.getById(operatorId);
        String operatorName = operator != null ? operator.getNickname() : "Unknown";

        // 获取旧值
        AgentRuntimeConfigEntity existing = agentRuntimeConfigInfra.getByKey(request.configKey());
        String oldValue = existing != null ? existing.getConfigValue() : null;

        // 更新配置
        if (existing != null) {
            agentRuntimeConfigInfra.updateConfigValue(request.configKey(), request.configValue());
        } else {
            // 如果配置不存在，插入新配置
            agentRuntimeConfigInfra.insertConfig(request.configKey(), request.configValue(), getDescription(request.configKey()));
        }

        // 记录变更历史
        agentRuntimeConfigHistoryInfra.recordChange(
                operatorId,
                operatorName,
                request.configKey(),
                oldValue,
                request.configValue(),
                request.changeReason()
        );

        return null;
    }

    private String getDescription(String configKey) {
        return switch (configKey) {
            case "llm_retry" -> "LLM重试与限流配置";
            case "context_compact" -> "上下文压缩配置";
            case "tool_result_compact" -> "工具结果压缩配置";
            case "memory_summary" -> "记忆摘要配置";
            case "embedding" -> "Embedding模型配置";
            case "runtime_basic" -> "基础运行时配置";
            default -> configKey;
        };
    }
}
