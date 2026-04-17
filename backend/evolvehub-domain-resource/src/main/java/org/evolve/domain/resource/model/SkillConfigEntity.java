package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 技能配置实体类
 *
 * @author zhao
 * @version v1.1
 * @date 2026/4/14
 */
@Getter
@Setter
@TableName("eh_skill_config")
public class SkillConfigEntity extends BaseEntity {

    /** 技能名称 */
    private String name;

    /** 技能描述 */
    private String description;

    /** 技能类型：CODER / WRITER / ANALYST 等 */
    private String skillType;

    /** SKILL.md 内容（Markdown 格式） */
    private String content;

    /** ZIP 包路径（上传模式） */
    private String packagePath;

    /** 来源：MANUAL-手动创建 HUB-Hub安装 BUILTIN-内置 */
    private String source;

    /** Hub 安装时的来源 URL */
    private String sourceUrl;

    /** 标签数组，如 ["coder", "document"] */
    private String tags;

    /** 配置信息（JSON） */
    private String config;

    /** 是否启用（1=启用 0=禁用） */
    private Integer enabled;

    /** 资源范围：SYSTEM-系统级 DEPT-部门级 USER-用户级 */
    private String scope;

    /** 部门ID，scope=DEPT 时必填 */
    private Long deptId;

    /** 创建者 ID，USER 时记录创建者（可见性由 eh_resource_grant 决定） */
    private Long ownerId;

    /** S3 工作区路径，如 skills/{id}/ 或 user/{userId}/skills/{id}/ */
    private String workspacePath;
}
