package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 添加安全扫描白名单请求
 */
public record AddSecurityScannerWhitelistRequest(
        @NotBlank(message = "技能名称不能为空")
        String skillName,

        @NotBlank(message = "内容哈希不能为空")
        String contentHash
) {}
