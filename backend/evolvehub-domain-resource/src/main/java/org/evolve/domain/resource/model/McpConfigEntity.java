package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * MCP 服务配置实体类
 *
 * @author zhao
 * @version v1.1
 * @date 2026/4/14
 */
@Getter
@Setter
@TableName("eh_mcp_config")
public class McpConfigEntity extends BaseEntity {

    /** 服务名称 */
    private String name;

    /** 服务描述 */
    private String description;

    /** 传输类型：UPLOADED-本地上传 REMOTE-远程URL */
    private String transportType;

    /** 服务器地址（REMOTE 模式必填） */
    private String serverUrl;

    /** zip 包存储路径（MinIO/S3，UPLOADED 模式使用） */
    private String packagePath;

    /** 启动命令（如 node server.js） */
    private String command;

    /** 命令参数数组 */
    private String args;

    /** 环境变量（JSON） */
    private String env;

    /** 工作目录（STDIO 模式） */
    private String workDir;

    /** 协议类型（STDIO / SSE，仅兼容性保留） */
    private String protocol;

    /** 配置信息（JSON） */
    private String config;

    /** 是否启用（1=启用 0=禁用） */
    private Integer enabled;

    /** 资源范围：SYSTEM-系统级 DEPT-部门级 USER-用户级 */
    private String scope;

    /** 部门ID，scope=DEPT 时必填 */
    private Long deptId;

    /** 创建者 ID，USER 时记录创建者（可见性由 eh_resource_grant 决定） */
    private Long ownerId;
}
