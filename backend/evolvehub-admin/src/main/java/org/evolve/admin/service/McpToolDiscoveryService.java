package org.evolve.admin.service;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.Resource;
import org.evolve.domain.resource.infra.McpToolInfra;
import org.evolve.domain.resource.model.McpToolEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MCP 工具发现服务
 *
 * 负责从 MCP Server 自动发现工具并保存到数据库
 *
 * @author devnomad-byte
 * @version v1.0
 * @date 2026/4/15
 */
@Service
public class McpToolDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(McpToolDiscoveryService.class);

    @Resource
    private McpToolInfra mcpToolInfra;

    /**
     * 发现并保存工具
     */
    public void discoverAndSaveTools(Long mcpConfigId, McpSyncClient client) {
        try {
            McpSchema.ListToolsResult result = client.listTools();
            log.info("发现 MCP 工具: configId={}, toolCount={}", mcpConfigId, result.tools().size());

            // 转换为实体并保存
            List<McpToolEntity> entities = result.tools().stream()
                    .map(tool -> convertToEntity(mcpConfigId, tool))
                    .toList();

            // 先删除旧工具
            deleteToolsByMcpConfigId(mcpConfigId);

            // 保存新工具
            saveTools(entities);

            log.info("MCP 工具保存成功: configId={}, toolCount={}", mcpConfigId, entities.size());

        } catch (Exception e) {
            log.error("MCP 工具发现失败: configId={}", mcpConfigId, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "工具发现失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发工具发现
     */
    public List<McpToolEntity> discoverTools(Long mcpConfigId, McpSyncClient client) {
        try {
            McpSchema.ListToolsResult result = client.listTools();
            log.info("手动触发 MCP 工具发现: configId={}, toolCount={}", mcpConfigId, result.tools().size());

            return result.tools().stream()
                    .map(tool -> convertToEntity(mcpConfigId, tool))
                    .toList();

        } catch (Exception e) {
            log.error("MCP 工具发现失败: configId={}", mcpConfigId, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "工具发现失败: " + e.getMessage());
        }
    }

    /**
     * 将 McpSchema.Tool 转换为实体
     */
    private McpToolEntity convertToEntity(Long mcpConfigId, McpSchema.Tool tool) {
        McpToolEntity entity = new McpToolEntity();
        entity.setMcpConfigId(mcpConfigId);
        entity.setName(tool.name());
        entity.setDescription(tool.description());

        // 转换 inputSchema 为 JSON
        if (tool.inputSchema() != null) {
            entity.setInputSchema(tool.inputSchema().toString());
        }

        // 评估风险等级
        entity.setRiskLevel(assessRiskLevel(tool.name(), tool.description()));

        // 默认设置
        entity.setToolScope("SYSTEM");
        entity.setEnabled(1);

        return entity;
    }

    /**
     * 评估工具风险等级
     */
    private String assessRiskLevel(String name, String description) {
        String lowerName = (name != null ? name : "").toLowerCase();
        String lowerDesc = (description != null ? description : "").toLowerCase();

        // 高风险：文件删除、系统命令、密码操作
        if (lowerName.contains("delete") || lowerName.contains("exec") ||
            lowerName.contains("password") || lowerName.contains(" rm ") ||
            lowerDesc.contains("delete") && lowerDesc.contains("file") ||
            lowerDesc.contains("run shell")) {
            return "HIGH";
        }

        // 中风险：文件写入、网络请求
        if (lowerName.contains("write") || lowerName.contains("upload") ||
            lowerName.contains("http") || lowerName.contains("post") ||
            lowerDesc.contains("modify") || lowerDesc.contains("create file")) {
            return "MEDIUM";
        }

        // 低风险：只读操作
        return "LOW";
    }

    /**
     * 删除 MCP 配置下的所有工具
     */
    public void deleteToolsByMcpConfigId(Long mcpConfigId) {
        List<McpToolEntity> existingTools = mcpToolInfra.listByMcpConfigId(mcpConfigId);
        for (McpToolEntity tool : existingTools) {
            mcpToolInfra.deleteTool(tool.getId());
        }
    }

    /**
     * 批量保存工具
     */
    public void saveTools(List<McpToolEntity> tools) {
        for (McpToolEntity tool : tools) {
            mcpToolInfra.saveTool(tool);
        }
    }

    /**
     * 获取 MCP 的工具列表
     */
    public List<McpToolEntity> getToolsByMcpConfigId(Long mcpConfigId) {
        return mcpToolInfra.listByMcpConfigId(mcpConfigId);
    }
}
