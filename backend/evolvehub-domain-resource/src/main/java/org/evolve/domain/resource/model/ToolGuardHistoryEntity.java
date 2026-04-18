package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 工具守卫阻断历史实体
 */
@Getter
@Setter
@TableName("eh_tool_guard_history")
public class ToolGuardHistoryEntity {

    /** 雪花ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 对话会话ID */
    private String sessionId;

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String userNickname;

    /** 工具名称 */
    private String toolName;

    /** 触发的参数名 */
    private String paramName;

    /** 匹配的规则ID */
    private String matchedRuleId;

    /** 匹配到的危险内容（脱敏） */
    private String matchedValue;

    /** 严重级别 */
    private String severity;

    /** 操作：BLOCKED/WARNED */
    private String action;

    /** 发生时间 */
    private LocalDateTime createTime;
}
