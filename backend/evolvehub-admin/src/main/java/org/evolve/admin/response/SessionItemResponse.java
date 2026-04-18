package org.evolve.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话项响应（含用户信息和模型信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionItemResponse {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String title;
    private Long modelConfigId;
    private String modelName;
    private Integer totalPromptTokens;
    private Integer totalCompletionTokens;
    private Integer totalTokens;
    private Integer messageCount;
    private String createTime;
    private String updateTime;
}
