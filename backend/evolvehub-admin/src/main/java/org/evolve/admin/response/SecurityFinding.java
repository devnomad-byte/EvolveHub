package org.evolve.admin.response;

import lombok.Builder;
import lombok.Data;

/**
 * 安全扫描单项发现
 */
@Data
@Builder
public class SecurityFinding {

    /** 严重程度：CRITICAL / HIGH / MEDIUM / LOW */
    private String severity;

    /** 问题标题 */
    private String title;

    /** 问题描述 */
    private String description;

    /** 文件路径 */
    private String filePath;

    /** 行号（可选） */
    private Integer lineNumber;

    /** 规则ID */
    private String ruleId;
}
