package org.evolve.admin.service;

import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import org.evolve.domain.resource.infra.McpInstanceInfra;
import org.evolve.domain.resource.model.McpInstanceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MCP 心跳检测服务
 *
 * 定期检查所有运行中的 MCP 实例是否存活
 *
 * @author devnomad-byte
 * @version v1.0
 * @date 2026/4/15
 */
@Service
public class McpHeartbeatService {

    private static final Logger log = LoggerFactory.getLogger(McpHeartbeatService.class);

    private static final int MAX_FAIL_COUNT = 3;  // 最大连续失败次数

    @Resource
    private McpInstanceInfra mcpInstanceInfra;

    @Resource
    private McpInstanceService mcpInstanceService;

    /**
     * 心跳检测定时任务 - 每 30 秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void checkHeartbeats() {
        List<McpInstanceEntity> runningInstances = mcpInstanceService.getAllRunningInstances();

        for (McpInstanceEntity instance : runningInstances) {
            try {
                boolean isHealthy = checkInstanceHealth(instance);
                if (isHealthy) {
                    // 健康：更新心跳时间
                    mcpInstanceInfra.updateHeartbeat(instance.getId());
                    instance.setLastHeartbeat(LocalDateTime.now());
                    instance.setFailCount(0);
                } else {
                    // 不健康：增加失败计数
                    handleUnhealthyInstance(instance);
                }
            } catch (Exception e) {
                log.error("心跳检测异常: instanceId={}", instance.getId(), e);
                handleUnhealthyInstance(instance);
            }
        }
    }

    /**
     * 检查实例是否健康
     */
    private boolean checkInstanceHealth(McpInstanceEntity instance) {
        McpSyncClient client = mcpInstanceService.getMcpClient(instance.getMcpConfigId());
        if (client == null) {
            log.warn("MCP 客户端未找到: configId={}", instance.getMcpConfigId());
            return false;
        }

        try {
            // 尝试列出工具来检查连接
            client.listTools();
            return true;

        } catch (Exception e) {
            log.warn("MCP 连接检查失败: configId={}, error={}", instance.getMcpConfigId(), e.getMessage());
            return false;
        }
    }

    /**
     * 处理不健康的实例
     */
    private void handleUnhealthyInstance(McpInstanceEntity instance) {
        int newFailCount = instance.getFailCount() + 1;
        instance.setFailCount(newFailCount);

        if (newFailCount >= MAX_FAIL_COUNT) {
            // 连续失败达到阈值，标记为 ERROR
            log.error("MCP 实例连续失败 {} 次，标记为 ERROR: configId={}",
                    MAX_FAIL_COUNT, instance.getMcpConfigId());
            instance.setStatus("ERROR");
            instance.setErrorMsg("连续心跳失败 " + MAX_FAIL_COUNT + " 次");
            mcpInstanceInfra.updateStatusAndError(instance.getId(), "ERROR",
                    "连续心跳失败 " + MAX_FAIL_COUNT + " 次");
        } else {
            // 更新失败计数
            mcpInstanceInfra.incrementFailCount(instance.getId(), newFailCount);
            log.warn("MCP 实例心跳失败: configId={}, failCount={}/{}",
                    instance.getMcpConfigId(), newFailCount, MAX_FAIL_COUNT);
        }
    }

    /**
     * 手动触发单实例心跳检测
     */
    public boolean checkInstance(Long mcpConfigId) {
        McpInstanceEntity instance = mcpInstanceService.getRunningInstance(mcpConfigId);
        if (instance == null) {
            return false;
        }

        boolean isHealthy = checkInstanceHealth(instance);
        if (isHealthy) {
            mcpInstanceInfra.updateHeartbeat(instance.getId());
            instance.setLastHeartbeat(LocalDateTime.now());
            instance.setFailCount(0);
        } else {
            handleUnhealthyInstance(instance);
        }

        return isHealthy;
    }
}
