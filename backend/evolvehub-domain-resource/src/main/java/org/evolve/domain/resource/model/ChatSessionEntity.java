package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 对话会话实体
 *
 * @author zhao
 * @date 2026/4/14
 */
@Getter
@Setter
@TableName("eh_chat_session")
public class ChatSessionEntity extends BaseEntity {

    private Long userId;
    private String title;
    private Long modelConfigId;
    private String sysPrompt;
    private Integer totalPromptTokens;
    private Integer totalCompletionTokens;
    private Integer totalTokens;
    private Integer messageCount;
    private String contextSummary;
    private Long deptId;
}
