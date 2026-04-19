package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.evolve.common.base.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eh_cron_job")
public class CronJobEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String description;

    private Integer enabled;

    private String cronExpression;

    private String timezone;

    private String taskType;

    private String promptTemplate;

    private Long targetUserId;

    private Long targetSessionId;

    private Integer timeoutSeconds;

    private Integer maxRetries;

    private Integer misfireGraceSeconds;

    private Integer maxConcurrency;

    private LocalDateTime lastRunTime;

    private LocalDateTime nextRunTime;

    private String lastRunStatus;

    private String lastRunError;

    private Long deptId;

    private Long createBy;
}
