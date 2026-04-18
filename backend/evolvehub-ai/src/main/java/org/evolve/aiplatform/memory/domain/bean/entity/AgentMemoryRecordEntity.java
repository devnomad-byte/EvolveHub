package org.evolve.aiplatform.memory.domain.bean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AgentScope 长期记忆记录实体
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Getter
@Setter
@TableName("eh_agent_memory_record")
public class AgentMemoryRecordEntity extends BaseEntity {

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
     * 部门 ID
     */
    @TableField("dept_id")
    private Long deptId;

    /*
     * 消息 ID
     */
    @TableField("message_id")
    private String messageId;

    /*
     * 记忆键
     */
    @TableField("memory_key")
    private String memoryKey;

    /*
     * 记忆类型
     */
    @TableField("memory_type")
    private String memoryType;

    /*
     * 来源类型
     */
    @TableField("source_kind")
    private String sourceKind;

    /*
     * 记忆归类
     */
    @TableField("memory_kind")
    private String memoryKind;

    /*
     * 角色
     */
    @TableField("role")
    private String role;

    /*
     * 模型配置 ID
     */
    @TableField("model_config_id")
    private Long modelConfigId;

    /*
     * 向量模型 ID
     */
    @TableField("embedding_model_id")
    private Long embeddingModelId;

    /*
     * Milvus 文档 ID
     */
    @TableField("vector_doc_id")
    private String vectorDocId;

    /*
     * 对象 ID
     */
    @TableField("object_id")
    private Long objectId;

    /*
     * 覆盖起始回合号
     */
    @TableField("round_start_no")
    private Integer roundStartNo;

    /*
     * 覆盖结束回合号
     */
    @TableField("round_end_no")
    private Integer roundEndNo;

    /*
     * 冷归档对象 ID
     */
    @TableField("archive_object_id")
    private Long archiveObjectId;

    /*
     * 摘要片段
     */
    @TableField("excerpt")
    private String excerpt;

    /*
     * 重要性
     */
    @TableField("importance")
    private BigDecimal importance;

    /*
     * 最后访问时间
     */
    @TableField("last_access_time")
    private LocalDateTime lastAccessTime;

    /*
     * 最近激活回合号
     */
    @TableField("last_activated_round_no")
    private Integer lastActivatedRoundNo;

    /*
     * 进入沉睡的回合阈值
     */
    @TableField("sleep_after_round_no")
    private Integer sleepAfterRoundNo;
}
