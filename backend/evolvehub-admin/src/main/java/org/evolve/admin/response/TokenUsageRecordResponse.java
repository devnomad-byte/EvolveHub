package org.evolve.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token用量记录响应（含用户信息和模型信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsageRecordResponse {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private Long deptId;
    private String deptName;
    private Long modelConfigId;
    private String modelName;
    private String usageDate;
    private Integer requestCount;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
}
