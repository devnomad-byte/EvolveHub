package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

import java.time.LocalDate;

/**
 * Token消费日报表实体
 *
 * @author zhao
 * @date 2026/4/18
 */
@Getter
@Setter
@TableName("eh_chat_token_usage")
public class ChatTokenUsageEntity extends BaseEntity {

    private Long userId;
    private Long modelConfigId;
    private LocalDate usageDate;
    private Integer requestCount;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Long deptId;
}
