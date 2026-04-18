package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 全局环境变量实体
 */
@Getter
@Setter
@TableName("eh_env_var")
public class EnvVarEntity extends BaseEntity {

    /** 变量名（唯一键） */
    private String varKey;

    /** 变量值 */
    private String varValue;

    /** 分组：OPENAI / MODELSCOPE / S3 / CUSTOM / DEFAULT */
    private String varGroup;

    /** 描述说明 */
    private String description;

    /** 是否敏感（1=敏感值，前端默认隐藏） */
    private Integer isSensitive;

    /** 状态（1=启用 0=禁用） */
    private Integer status;

    /** 排序 */
    private Integer sort;
}
