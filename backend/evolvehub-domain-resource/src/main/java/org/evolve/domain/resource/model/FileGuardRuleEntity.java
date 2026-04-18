package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感文件保护规则实体
 */
@Data
@TableName("eh_file_guard_rule")
public class FileGuardRuleEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 规则唯一标识 */
    private String ruleId;

    /** 规则名称 */
    private String name;

    /** 路径模式（支持目录路径、通配符） */
    private String pathPattern;

    /** 路径类型：FILE=精确文件，DIRECTORY=目录，WILDCARD=通配符 */
    private String pathType;

    /** 适用的工具列表，JSON数组，null表示全部工具 */
    private String tools;

    /** 规则描述 */
    private String description;

    /** 修复建议 */
    private String remediation;

    /** 严重级别：CRITICAL, HIGH, MEDIUM, LOW, INFO */
    private String severity;

    /** 是否内置规则：0=自定义，1=内置 */
    private Integer isBuiltin;

    /** 是否启用：0=禁用，1=启用 */
    private Integer enabled;

    /** 创建人 */
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记 */
    private Integer deleted;
}
