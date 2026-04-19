package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.evolve.common.base.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eh_cron_job_history")
public class CronJobHistoryEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long jobId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private String triggerType;

    private String sessionId;

    private String promptContent;

    private String responseContent;

    private String errorMessage;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;
}
