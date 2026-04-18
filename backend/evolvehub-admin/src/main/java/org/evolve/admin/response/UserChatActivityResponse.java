package org.evolve.admin.response;

import lombok.Builder;
import lombok.Data;

/**
 * 用户聊天活动聚合响应
 * <p>
 * 包含用户基本信息 + 聊天活跃数据（用于对话历史管理的用户列表）
 */
@Data
@Builder
public class UserChatActivityResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Long deptId;
    private String deptName;
    private String lastActiveTime;
    private Integer sessionCount;
}
