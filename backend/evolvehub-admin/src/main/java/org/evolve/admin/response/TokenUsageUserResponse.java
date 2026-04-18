package org.evolve.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户Token用量聚合响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsageUserResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Long deptId;
    private String deptName;
    private Long totalRequests;
    private Long totalPromptTokens;
    private Long totalCompletionTokens;
    private Long totalTokens;
}
