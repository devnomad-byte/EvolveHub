package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * MCP 服务配置实体类
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Getter
@Setter
@TableName("eh_mcp_config")
public class McpConfigEntity extends BaseEntity {

    /** 服务名称 */
    private String name;

    /** 服务描述 */
    private String description;

    /** 服务地址 */
    private String serverUrl;

    /** 传输协议（stdio / sse） */
    private String protocol;

    /** 配置信息（JSON） */
    private String config;

    /** 是否启用（1=启用 0=禁用） */
    private Integer enabled;

    /** 资源范围：SYSTEM-系统级 USER-用户级 */
    private String scope;

    /** 资源所有者 ID，scope=SYSTEM 时为 NULL */
    private Long ownerId;
}
