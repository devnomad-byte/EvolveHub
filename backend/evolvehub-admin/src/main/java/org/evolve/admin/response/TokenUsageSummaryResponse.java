package org.evolve.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token用量汇总响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsageSummaryResponse {
    private Long totalRequests;
    private Long totalPromptTokens;
    private Long totalCompletionTokens;
    private Long totalTokens;
}
