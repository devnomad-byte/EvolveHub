package org.evolve.aiplatform.memory.domain.bean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

import java.math.BigDecimal;
import java.lang.Integer;

/**
 * 用户结构化记忆实体
 *
 * @author TellyJiang
 * @since 2026-04-18
 */
@Getter
@Setter
@TableName("user_memory")
public class UserMemoryEntity extends BaseEntity {

    /*
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /*
     * 部门 ID
     */
    @TableField("dept_id")
    private Long deptId;

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
     * 记忆内容
     */
    @TableField("content")
    private String content;

    /*
     * 重要性
     */
    @TableField("importance")
    private BigDecimal importance;

    /*
     * 向量文档 ID
     */
    @TableField("vector_doc_id")
    private String vectorDocId;

    /*
     * 会话 ID
     */
    @TableField("session_id")
    private String sessionId;

    /*
     * 摘要结束回合
     */
    @TableField(exist = false)
    private Integer roundEndNo;
}
