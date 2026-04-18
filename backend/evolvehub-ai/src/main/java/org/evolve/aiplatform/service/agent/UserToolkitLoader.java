package org.evolve.aiplatform.service.agent;

import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.evolve.aiplatform.memory.application.tool.MemoryTools;
import org.evolve.domain.resource.infra.McpConfigInfra;
import org.evolve.domain.resource.infra.ResourceGrantInfra;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户工具集加载器
 * <p>
 * 根据当前用户的资源授权，加载其可用的 MCP 服务和 Skill 技能，注册到 AgentScope Toolkit。
 * 加载范围：用户自建的（scope=USER, ownerId=当前用户）+ 管理员授权的系统级资源（通过 eh_resource_grant）。
 * </p>
 *
 * @author zhao
 */
@Component
public class UserToolkitLoader {

    private static final Logger log = LoggerFactory.getLogger(UserToolkitLoader.class);

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Resource
    private ResourceGrantInfra resourceGrantInfra;

    @Resource
    private MemoryApi memoryApi;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 为指定用户构建 Toolkit（含记忆工具 + MCP 服务 + Skill 技能）
     *
     * @param userId    当前用户 ID
     * @param sessionId 当前会话 ID
     * @return 已注册完所有工具的 Toolkit
     */
    public Toolkit loadToolkit(Long userId, Long sessionId) {
        Toolkit toolkit = new Toolkit();

        // 1. 注册记忆工具（所有用户都有）
        MemoryTools memoryTools = new MemoryTools(memoryApi, userId, sessionId);
        toolkit.registerTool(memoryTools);
        log.debug("已注册记忆工具: userId={}", userId);

        // 2. 注册用户画像工具（所有用户都有）
        UserProfileTools profileTools = new UserProfileTools(memoryApi, userId);
        toolkit.registerTool(profileTools);
        log.debug("已注册用户画像工具: userId={}", userId);

        // 3. 加载用户可用的 MCP 服务
        try {
            List<McpConfigEntity> mcpConfigs = loadUserMcpConfigs(userId);
            for (McpConfigEntity mcp : mcpConfigs) {
                try {
                    McpClientWrapper client = buildMcpClient(mcp);
                    toolkit.registerMcpClient(client).block();
                    log.info("已注册 MCP 服务: name={}, protocol={}, userId={}", mcp.getName(), mcp.getProtocol(), userId);
                } catch (Exception e) {
                    log.warn("注册 MCP 服务失败: name={}, error={}", mcp.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("加载 MCP 服务配置失败，跳过工具注册: userId={}, error={}", userId, e.getMessage());
        }

        // 4. 加载用户可用的 Skill 技能（Skill 暂按 MCP 协议注册，后续可扩展为原生 @Tool 注册）
        try {
            List<SkillConfigEntity> skillConfigs = loadUserSkillConfigs(userId);
            for (SkillConfigEntity skill : skillConfigs) {
                try {
                    McpClientWrapper client = buildSkillAsClient(skill);
                    if (client != null) {
                        toolkit.registerMcpClient(client).block();
                        log.info("已注册 Skill 技能: name={}, userId={}", skill.getName(), userId);
                    }
                } catch (Exception e) {
                    log.warn("注册 Skill 技能失败: name={}, error={}", skill.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("加载 Skill 配置失败，跳过技能注册: userId={}, error={}", userId, e.getMessage());
        }

        return toolkit;
    }

    /**
     * 加载当前用户可用的全部 MCP 配置（自建 + 系统授权）
     */
    private List<McpConfigEntity> loadUserMcpConfigs(Long userId) {

        // 用户自建的已启用 MCP
        List<McpConfigEntity> result = new ArrayList<>(mcpConfigInfra.listEnabledByOwnerId(userId));

        // 管理员授权的系统级 MCP
        List<Long> grantedMcpIds = resourceGrantInfra.listGrantedResourceIds(userId, "MCP");
        if (!grantedMcpIds.isEmpty()) {
            result.addAll(mcpConfigInfra.listByIdsAndScope(grantedMcpIds));
        }

        return result;
    }

    /**
     * 加载当前用户可用的全部 Skill 配置（自建 + 系统授权）
     */
    private List<SkillConfigEntity> loadUserSkillConfigs(Long userId) {

        // 用户自建的已启用 Skill
        List<SkillConfigEntity> result = new ArrayList<>(skillConfigInfra.listEnabledByOwnerId(userId));

        // 管理员授权的系统级 Skill
        List<Long> grantedSkillIds = resourceGrantInfra.listGrantedResourceIds(userId, "SKILL");
        if (!grantedSkillIds.isEmpty()) {
            result.addAll(skillConfigInfra.listByIdsAndScope(grantedSkillIds));
        }

        return result;
    }

    /**
     * 根据 MCP 配置构建 McpClientWrapper
     *
     * @param mcp MCP 配置实体
     * @return MCP 客户端
     */
    private McpClientWrapper buildMcpClient(McpConfigEntity mcp) {
        McpClientBuilder builder = McpClientBuilder.create(mcp.getName());

        return switch (mcp.getProtocol().toLowerCase()) {
            case "sse" -> builder.sseTransport(mcp.getServerUrl()).buildSync();
            case "streamable_http", "streamablehttp" -> builder.streamableHttpTransport(mcp.getServerUrl()).buildSync();
            case "stdio" -> {
                // stdio 模式：serverUrl 为命令，config 中存放 args（JSON 数组）
                String command = mcp.getServerUrl();
                yield builder.stdioTransport(command).buildSync();
            }
            default -> throw new IllegalArgumentException("不支持的 MCP 协议: " + mcp.getProtocol());
        };
    }

    /**
     * 将 Skill 作为 MCP 客户端注册（Skill 底层也走 MCP 协议）
     *
     * @param skill Skill 配置实体
     * @return MCP 客户端，如果 Skill 类型不支持则返回 null
     */
    private McpClientWrapper buildSkillAsClient(SkillConfigEntity skill) {
        // Skill 的 config 字段存放 JSON，包含 serverUrl 和 protocol
        if (skill.getConfig() == null || skill.getConfig().isBlank()) {
            log.warn("Skill 配置信息为空: name={}", skill.getName());
            return null;
        }

        try {
            var configNode = objectMapper.readTree(skill.getConfig());
            String serverUrl = configNode.has("serverUrl") ? configNode.get("serverUrl").asText() : null;
            String protocol = configNode.has("protocol") ? configNode.get("protocol").asText() : "sse";

            if (serverUrl == null || serverUrl.isBlank()) {
                log.warn("Skill 缺少 serverUrl: name={}", skill.getName());
                return null;
            }

            McpClientBuilder builder = McpClientBuilder.create(skill.getName());
            return switch (protocol.toLowerCase()) {
                case "sse" -> builder.sseTransport(serverUrl).buildSync();
                case "streamable_http", "streamablehttp" -> builder.streamableHttpTransport(serverUrl).buildSync();
                default -> {
                    log.warn("Skill 不支持的协议: name={}, protocol={}", skill.getName(), protocol);
                    yield null;
                }
            };
        } catch (Exception e) {
            log.warn("解析 Skill 配置失败: name={}, error={}", skill.getName(), e.getMessage());
            return null;
        }
    }
}
