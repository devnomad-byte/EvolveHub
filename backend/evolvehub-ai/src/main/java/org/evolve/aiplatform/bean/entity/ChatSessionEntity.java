package org.evolve.aiplatform.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 对话会话实体
 * <p>
 * 对应数据库表 eh_chat_session，记录用户的一次对话会话。
 * 每个会话绑定一个模型配置，包含累计 token 消耗统计。
 * </p>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/14
 */
@Getter
@Setter
@TableName("eh_chat_session")
public class ChatSessionEntity extends BaseEntity {

    /**
     * 所属用户 ID
     */
    private Long userId;

    /**
     * 会话标题（首轮自动生成，可手动修改）
     */
    private String title;

    /**
     * 使用的模型配置 ID（关联 eh_model_config）
     */
    private Long modelConfigId;

    /**
     * 自定义系统提示词（空则使用默认）
     */
    private String sysPrompt;

    /**
     * 累计 prompt token 数
     */
    private Integer totalPromptTokens;

    /**
     * 累计 completion token 数
     */
    private Integer totalCompletionTokens;

    /**
     * 累计总 token 数
     */
    private Integer totalTokens;

    /**
     * 消息条数
     */
    private Integer messageCount;

    /**
     * 短期记忆压缩摘要（滑动窗口外旧消息的 LLM 摘要）
     */
    private String contextSummary;
}
