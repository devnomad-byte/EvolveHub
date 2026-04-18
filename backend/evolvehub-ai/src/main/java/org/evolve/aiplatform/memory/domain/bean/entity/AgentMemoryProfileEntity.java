package org.evolve.aiplatform.memory.domain.bean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * AgentScope 用户画像索引实体
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Getter
@Setter
@TableName("eh_agent_memory_profile")
public class AgentMemoryProfileEntity extends BaseEntity {

    /*
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /*
     * 画像对象 ID
     */
    @TableField("profile_object_id")
    private Long profileObjectId;

    /*
     * 画像摘要
     */
    @TableField("profile_summary")
    private String profileSummary;

    /*
     * 姓名
     */
    @TableField("name")
    private String name;

    /*
     * 部门
     */
    @TableField("department")
    private String department;

    /*
     * 偏好语言
     */
    @TableField("preferred_language")
    private String preferredLanguage;

    /*
     * 偏好模型
     */
    @TableField("preferred_model")
    private String preferredModel;

    /*
     * 工具偏好
     */
    @TableField("tool_preference")
    private String toolPreference;

    /*
     * 最近提取时间
     */
    @TableField("last_extracted_time")
    private LocalDateTime lastExtractedTime;
}
