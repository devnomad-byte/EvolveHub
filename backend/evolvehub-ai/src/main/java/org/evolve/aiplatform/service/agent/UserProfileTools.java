package org.evolve.aiplatform.service.agent;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import org.evolve.aiplatform.memory.application.service.MemoryApi;
import org.evolve.common.base.CurrentUserHolder;

import java.util.function.Supplier;

/**
 * 用户画像工具（供 Agent 调用）
 * <p>
 * 允许 Agent 读取和更新用户的个人画像（MEMORY.md），
 * 存储在 MinIO 中，跨会话持久化。
 * </p>
 *
 * @author zhao
 */
public record UserProfileTools(MemoryApi memoryApi, Long userId) {

    /**
     * 读取用户画像
     */
    @Tool(name = "read_user_profile", description = "读取当前用户的个人画像文件（MEMORY.md），包含用户偏好、背景等持久化信息")
    public String readProfile() {
        String profile = withUserContext(() -> memoryApi.getMemoryProfile(userId));
        return (profile != null && !profile.isBlank()) ? profile : "用户画像为空，暂无记录。";
    }

    /**
     * 更新用户画像（全量覆盖）
     */
    @Tool(name = "update_user_profile", description = "更新当前用户的个人画像文件（MEMORY.md），用 Markdown 格式描述用户的偏好、背景、工作信息等。会覆盖原有内容。")
    public String updateProfile(
            @ToolParam(name = "content", description = "新的用户画像内容（Markdown 格式）") String content) {
        withUserContext(() -> memoryApi.saveMemoryProfileMarkdown(userId, content));
        return "用户画像已更新。";
    }

    /**
     * 追加内容到用户画像
     */
    @Tool(name = "append_user_profile", description = "向当前用户的个人画像文件追加新内容，不会覆盖原有信息")
    public String appendProfile(
            @ToolParam(name = "content", description = "要追加的画像内容") String content) {
        withUserContext(() -> memoryApi.appendMemoryProfileMarkdown(userId, content));
        return "已追加到用户画像。";
    }

    private <T> T withUserContext(Supplier<T> action) {
        Long previousUserId = CurrentUserHolder.getUserId();
        try {
            CurrentUserHolder.set(userId);
            return action.get();
        } finally {
            if (previousUserId == null) {
                CurrentUserHolder.clear();
            } else {
                CurrentUserHolder.set(previousUserId);
            }
        }
    }
}
