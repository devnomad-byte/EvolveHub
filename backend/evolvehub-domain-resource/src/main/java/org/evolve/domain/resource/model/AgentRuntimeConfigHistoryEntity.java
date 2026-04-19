package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.evolve.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * Agent运行时配置变更历史实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eh_agent_runtime_config_history")
public class AgentRuntimeConfigHistoryEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 旧值
     */
    private String oldValue;

    /**
     * 新值
     */
    private String newValue;

    /**
     * 变更原因
     */
    private String changeReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
