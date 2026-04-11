package org.evolve.aiplatform.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.aiplatform.bean.enums.KbLevel;
import org.evolve.common.base.BaseEntity;

/**
 * 知识库实体
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Getter
@Setter
@TableName("eh_knowledge_base")
public class KnowledgeBaseEntity extends BaseEntity {

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 权限级别（GLOBAL=1 DEPT=2 PROJECT=3 SENSITIVE=4）
     */
    private KbLevel level;

    /**
     * 所属部门 ID（level=2 时有效）
     */
    private Long deptId;

    /**
     * 所有者用户 ID
     */
    private Long ownerId;

    /**
     * 知识库描述
     */
    private String description;
}