package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.evolve.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * Agent运行时配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eh_agent_runtime_config")
public class AgentRuntimeConfigEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值（JSON格式）
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String description;
}
