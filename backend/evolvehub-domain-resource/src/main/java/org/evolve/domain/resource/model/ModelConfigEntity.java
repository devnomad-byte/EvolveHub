package org.evolve.domain.resource.model;

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

    /**
     * 资源范围：SYSTEM-系统级 DEPT-部门级 USER-指定用户级
     */
    private String scope;

    /**
     * 部门 ID，scope=DEPT 时必填
     */
    private Long deptId;

    /**
     * 创建者 ID，USER 时记录创建者（可见性由 eh_resource_grant 决定）
     */
    private Long ownerId;

}
