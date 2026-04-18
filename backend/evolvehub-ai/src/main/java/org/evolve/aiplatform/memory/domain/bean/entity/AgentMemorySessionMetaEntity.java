package org.evolve.aiplatform.memory.domain.bean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * AgentScope 会话元数据实体
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Getter
@Setter
@TableName("eh_agent_memory_session_meta")
public class AgentMemorySessionMetaEntity extends BaseEntity {

    /*
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /*
     * 会话 ID
     */
    @TableField("session_id")
    private String sessionId;

    /*
     * 会话键
     */
    @TableField("session_key")
    private String sessionKey;

    /*
     * 模型名称
     */
    @TableField("model_name")
    private String modelName;

    /*
     * 消息数量
     */
    @TableField("message_count")
    private Integer messageCount;

    /*
     * 最后摘要对象 ID
     */
    @TableField("last_summary_object_id")
    private Long lastSummaryObjectId;

    /*
     * Transcript 对象 ID
     */
    @TableField("transcript_object_id")
    private Long transcriptObjectId;

    /*
     * 当前最新回合号
     */
    @TableField("current_round_no")
    private Integer currentRoundNo;

    /*
     * 最近一次压缩到的回合号
     */
    @TableField("last_compacted_round_no")
    private Integer lastCompactedRoundNo;

    /*
     * 最近一次快照对象 ID
     */
    @TableField("last_snapshot_object_id")
    private Long lastSnapshotObjectId;

    /*
     * 状态
     */
    @TableField("status")
    private String status;

    /*
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /*
     * 最后消息时间
     */
    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;
}
