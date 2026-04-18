package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.DeptEntity;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.domain.rbac.service.UserDataScopeService;
import org.evolve.domain.resource.infra.ChatMessageInfra;
import org.evolve.domain.resource.infra.ChatSessionInfra;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ChatMessageEntity;
import org.evolve.domain.resource.model.ChatSessionEntity;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理员导出对话历史
 */
@Service
public class ChatHistoryExportManager extends BaseManager<ChatHistoryExportManager.Request, ChatHistoryExportManager.ExportResult> {

    public record Request(
            Long userId,
            String startDate,
            String endDate,
            String format
    ) {}

    public record ExportResult(
            String filename,
            String content
    ) {}

    @Resource
    private ChatSessionInfra chatSessionInfra;

    @Resource
    private ChatMessageInfra chatMessageInfra;

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UserDataScopeService userDataScopeService;

    @Override
    protected void check(Request request) {
        // format默认为md，无需校验
    }

    @Override
    protected ExportResult process(Request request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        var scopeInfo = userDataScopeService.getDataScopeInfo(currentUserId);

        // 查询会话列表
        var page = chatSessionInfra.listPageByDataScope(
                scopeInfo.dataScope(),
                scopeInfo.deptId(),
                scopeInfo.visibleDeptIds(),
                currentUserId,
                request.userId(),
                null,
                request.startDate(),
                request.endDate(),
                1,
                100);

        if (page.getRecords().isEmpty()) {
            return new ExportResult("chat_history.md", "# 无对话记录\n\n暂无可导出的对话记录。");
        }

        // 收集用户和模型信息
        Set<Long> userIds = page.getRecords().stream()
                .map(ChatSessionEntity::getUserId)
                .filter(u -> u != null)
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> modelIds = page.getRecords().stream()
                .map(ChatSessionEntity::getModelConfigId)
                .filter(m -> m != null)
                .collect(java.util.stream.Collectors.toSet());

        Map<Long, UsersEntity> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UsersEntity u : usersInfra.listByIds(userIds)) {
                userMap.put(u.getId(), u);
            }
        }

        Map<Long, ModelConfigEntity> modelMap = new HashMap<>();
        if (!modelIds.isEmpty()) {
            for (ModelConfigEntity m : modelConfigInfra.listByIds(modelIds)) {
                modelMap.put(m.getId(), m);
            }
        }

        // 生成 Markdown
        StringBuilder md = new StringBuilder();
        md.append("# 对话历史导出\n\n");
        md.append("> 导出时间：").append(java.time.LocalDateTime.now().toString().replace("T", " ")).append("\n\n");
        md.append("---\n\n");

        for (ChatSessionEntity session : page.getRecords()) {
            UsersEntity user = userMap.get(session.getUserId());
            ModelConfigEntity model = modelMap.get(session.getModelConfigId());
            DeptEntity dept = user != null && user.getDeptId() != null
                    ? deptInfra.getDeptById(user.getDeptId()) : null;

            md.append("## ").append(session.getTitle() != null ? session.getTitle() : "未命名会话").append("\n\n");
            md.append("- **用户**：").append(user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "未知").append("\n");
            md.append("- **部门**：").append(dept != null ? dept.getDeptName() : "未分配").append("\n");
            md.append("- **模型**：").append(model != null ? model.getName() : "未知").append("\n");
            md.append("- **创建时间**：").append(session.getCreateTime() != null ? session.getCreateTime().toString().replace("T", " ") : "").append("\n");
            md.append("- **最后更新**：").append(session.getUpdateTime() != null ? session.getUpdateTime().toString().replace("T", " ") : "").append("\n");
            md.append("- **消息数**：").append(session.getMessageCount()).append("\n");
            md.append("- **Tokens**：").append(session.getTotalTokens()).append("\n\n");

            // 查询消息列表
            var messages = chatMessageInfra.listBySessionId(session.getId());
            if (!messages.isEmpty()) {
                md.append("### 对话内容\n\n");
                for (ChatMessageEntity msg : messages) {
                    String roleName = switch (msg.getRole()) {
                        case "user" -> "用户";
                        case "assistant" -> "助手";
                        case "system" -> "系统";
                        case "tool" -> "工具";
                        default -> msg.getRole();
                    };

                    md.append("**").append(roleName);
                    if (msg.getModelName() != null) {
                        md.append(" (").append(msg.getModelName()).append(")");
                    }
                    if (msg.getPromptTokens() != null && msg.getPromptTokens() > 0) {
                        md.append(" [").append(msg.getPromptTokens()).append(" tokens]");
                    }
                    md.append("]**\n\n");
                    md.append(msg.getContent() != null ? msg.getContent().replace("<", "&lt;").replace(">", "&gt;") : "").append("\n\n");

                    if (msg.getDurationMs() != null) {
                        md.append("> 耗时：").append(msg.getDurationMs()).append("ms");
                        if (msg.getCompletionTokens() != null && msg.getCompletionTokens() > 0) {
                            md.append(" | 输出 tokens：").append(msg.getCompletionTokens());
                        }
                        md.append("\n\n");
                    }
                }
            }

            md.append("---\n\n");
        }

        String filename = "chat_history_" + java.time.LocalDateTime.now().toString().substring(0, 10) + ".md";
        return new ExportResult(filename, md.toString());
    }
}
