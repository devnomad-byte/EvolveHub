package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * MCP 服务实例实体类
 *
 * 记录运行中的 MCP Server 实例，包括进程信息、状态、心跳时间等
 *
 * @author devnomad-byte
 * @version v1.0
 * @date 2026/4/15
 */
@Getter
@Setter
@TableName("eh_mcp_instance")
public class McpInstanceEntity extends BaseEntity {

    /** 关联的 MCP Config ID */
    private Long mcpConfigId;

    /** 唯一实例标识 (UUID) */
    private String instanceKey;

    /** OS 进程 ID (STDIO 模式) */
    private Long processId;

    /** 服务器地址 (SSE 模式) */
    private String serverUrl;

    /** 实例状态：STARTING/RUNNING/STOPPING/STOPPED/ERROR */
    private String status;

    /** 上次心跳时间 */
    private LocalDateTime lastHeartbeat;

    /** 启动时间 */
    private LocalDateTime startTime;

    /** 停止时间 */
    private LocalDateTime stopTime;

    /** 连续心跳失败次数 */
    private Integer failCount;

    /** 错误信息 */
    private String errorMsg;

    /** 传输类型：STDIO/SSE */
    private String transportType;

    /** 工作目录 (STDIO 模式) */
    private String workDir;

    /** 工具数量 */
    private Integer toolCount;
}
