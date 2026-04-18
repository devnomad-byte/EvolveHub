package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 工具守卫规则实体
 */
@Getter
@Setter
@TableName("eh_tool_guard_rule")
public class ToolGuardRuleEntity extends BaseEntity {

    /** 规则唯一标识 */
    private String ruleId;

    /** 规则中文名称 */
    private String name;

    /** 适用的工具列表，JSON数组 */
    private String tools;

    /** 需要检查的参数名，JSON数组 */
    private String params;

    /** 威胁类别 */
    private String category;

    /** 严重级别：CRITICAL/HIGH/MEDIUM/LOW/INFO */
    private String severity;

    /** 正则表达式列表，JSON数组 */
    private String patterns;

    /** 排除正则，JSON数组 */
    private String excludePatterns;

    /** 规则描述 */
    private String description;

    /** 处理建议 */
    private String remediation;

    /** 是否内置规则（1=内置，0=自定义） */
    private Integer isBuiltin;

    /** 是否启用（1=启用，0=禁用） */
    private Integer enabled;
}
