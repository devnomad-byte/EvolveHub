package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 模型提供商实体类
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Getter
@Setter
@TableName("eh_model_provider")
public class ModelProviderEntity extends BaseEntity {

    /**
     * 提供商名称（如 OpenAI / DeepSeek / Anthropic）
     */
    private String name;

    /**
     * 厂商 Logo URL
     */
    private String logoUrl;

    /**
     * 默认 Base URL
     */
    private String defaultBaseUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态（1=启用 0=禁用）
     */
    private Integer enabled;
}
