package org.evolve.aiplatform.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

import java.time.LocalDate;

/**
 * Token 消费日报实体
 * <p>
 * 对应数据库表 eh_chat_token_usage，按用户 + 模型 + 日期维度聚合统计 token 消耗。
 * 唯一约束：(user_id, model_config_id, usage_date)。
 * </p>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/14
 */
@Getter
@Setter
@TableName("eh_chat_token_usage")
public class ChatTokenUsageEntity extends BaseEntity {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 模型配置 ID
     */
    private Long modelConfigId;

    /**
     * 统计日期
     */
    private LocalDate usageDate;

    /**
     * 当日请求次数
     */
    private Integer requestCount;

    /**
     * 当日 prompt token 数
     */
    private Integer promptTokens;

    /**
     * 当日 completion token 数
     */
    private Integer completionTokens;

    /**
     * 当日总 token 数
     */
    private Integer totalTokens;
}
