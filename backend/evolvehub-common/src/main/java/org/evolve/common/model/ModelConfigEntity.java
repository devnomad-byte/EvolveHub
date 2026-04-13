package org.evolve.common.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * @className ModelConfigEntity
 * @description 模型配置实体类
 * @author zhao
 * @date 2026/4/10 12:41
 * @version v1.0
**/
@Getter
@Setter
@TableName("eh_model_config")
public class ModelConfigEntity extends BaseEntity {

    /**
     * 模型名称
     */
    private String name;

    /**
     * 提供商
     */
    private String provider;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 模型URL
     */
    private String baseUrl;

    /**
     * 是否启用
     */
    private Integer enabled;

    /**
     * 模型类型
     */
    private String modelType;

}
