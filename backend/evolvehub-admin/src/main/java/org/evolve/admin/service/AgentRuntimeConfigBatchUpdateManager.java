package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.evolve.admin.request.BatchUpdateAgentRuntimeConfigRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.AgentRuntimeConfigHistoryInfra;
import org.evolve.domain.resource.infra.AgentRuntimeConfigInfra;
import org.evolve.domain.resource.model.AgentRuntimeConfigEntity;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 批量更新Agent运行时配置
 */
@Service
public class AgentRuntimeConfigBatchUpdateManager extends BaseManager<BatchUpdateAgentRuntimeConfigRequest, Void> {

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
    protected void check(BatchUpdateAgentRuntimeConfigRequest request) {
        for (String configKey : request.configs().keySet()) {
            if (!ALLOWED_CONFIG_KEYS.contains(configKey)) {
                throw new org.evolve.common.web.exception.BusinessException(
                        org.evolve.common.web.response.ResultCode.BAD_REQUEST, "非法的配置键: " + configKey);
            }

            try {
                objectMapper.readTree(request.configs().get(configKey));
            } catch (JsonProcessingException e) {
                throw new org.evolve.common.web.exception.BusinessException(
                        org.evolve.common.web.response.ResultCode.BAD_REQUEST,
                        "配置键 " + configKey + " 的值必须是有效的JSON格式");
            }
        }
    }

    @Override
    protected Void process(BatchUpdateAgentRuntimeConfigRequest request) {
        // 获取当前登录用户信息
        Long operatorId = StpUtil.getLoginIdAsLong();
        UsersEntity operator = usersInfra.getById(operatorId);
        String operatorName = operator != null ? operator.getNickname() : "Unknown";

        // 更新每个配置
        for (var entry : request.configs().entrySet()) {
            String configKey = entry.getKey();
            String newValue = entry.getValue();

            // 获取旧值
            AgentRuntimeConfigEntity existing = agentRuntimeConfigInfra.getByKey(configKey);
            String oldValue = existing != null ? existing.getConfigValue() : null;

            // 更新配置
            if (existing != null) {
                agentRuntimeConfigInfra.updateConfigValue(configKey, newValue);
            } else {
                agentRuntimeConfigInfra.insertConfig(configKey, newValue, getDescription(configKey));
            }

            // 记录变更历史
            agentRuntimeConfigHistoryInfra.recordChange(
                    operatorId,
                    operatorName,
                    configKey,
                    oldValue,
                    newValue,
                    request.changeReason()
            );
        }

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
