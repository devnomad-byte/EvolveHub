package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.admin.response.TokenUsageSummaryResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.DeptInfra;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.rbac.model.DeptEntity;
import org.evolve.domain.rbac.model.UsersEntity;
import org.evolve.domain.rbac.service.UserDataScopeService;
import org.evolve.domain.resource.infra.ChatTokenUsageInfra;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ChatTokenUsageEntity;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 管理员导出Token用量
 */
@Service
public class TokenUsageExportManager extends BaseManager<TokenUsageExportManager.Request, TokenUsageExportManager.ExportResult> {

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
    private ChatTokenUsageInfra chatTokenUsageInfra;

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private DeptInfra deptInfra;

    @Resource
    private UserDataScopeService userDataScopeService;

    @Override
    protected void check(Request request) {}

    @Override
    protected ExportResult process(Request request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        var scopeInfo = userDataScopeService.getDataScopeInfo(currentUserId);

        // 查询用量记录
        Set<Long> userIds = null;
        if (request.userId() != null) {
            userIds = Set.of(request.userId());
        } else if (scopeInfo.dataScope() == 4) {
            userIds = Set.of(currentUserId);
        }

        List<ChatTokenUsageEntity> usageList;
        if (userIds != null && !userIds.isEmpty()) {
            usageList = chatTokenUsageInfra.listByUserIds(userIds, request.startDate(), request.endDate());
        } else {
            var page = chatTokenUsageInfra.listPageByDataScope(
                    scopeInfo.dataScope(),
                    scopeInfo.deptId(),
                    scopeInfo.visibleDeptIds(),
                    currentUserId,
                    null, null,
                    request.startDate(),
                    request.endDate(),
                    1,
                    10000);
            usageList = page.getRecords();
        }

        if (usageList.isEmpty()) {
            return new ExportResult("token_usage.md", "# 无用量记录\n\n暂无可导出的用量记录。");
        }

        // 收集用户和模型信息
        Set<Long> uIds = usageList.stream()
                .map(ChatTokenUsageEntity::getUserId)
                .filter(u -> u != null)
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> mIds = usageList.stream()
                .map(ChatTokenUsageEntity::getModelConfigId)
                .filter(m -> m != null)
                .collect(java.util.stream.Collectors.toSet());

        Map<Long, UsersEntity> userMap = new HashMap<>();
        if (!uIds.isEmpty()) {
            for (UsersEntity u : usersInfra.listByIds(uIds)) {
                userMap.put(u.getId(), u);
            }
        }

        Map<Long, ModelConfigEntity> modelMap = new HashMap<>();
        if (!mIds.isEmpty()) {
            for (ModelConfigEntity m : modelConfigInfra.listByIds(mIds)) {
                modelMap.put(m.getId(), m);
            }
        }

        // 汇总统计
        long totalRequests = 0, totalPrompt = 0, totalCompletion = 0, totalTokens = 0;
        for (ChatTokenUsageEntity u : usageList) {
            totalRequests += u.getRequestCount() != null ? u.getRequestCount() : 0;
            totalPrompt += u.getPromptTokens() != null ? u.getPromptTokens() : 0;
            totalCompletion += u.getCompletionTokens() != null ? u.getCompletionTokens() : 0;
            totalTokens += u.getTotalTokens() != null ? u.getTotalTokens() : 0;
        }

        // 生成 Markdown
        StringBuilder md = new StringBuilder();
        md.append("# Token 用量导出\n\n");
        md.append("> 导出时间：").append(java.time.LocalDateTime.now().toString().replace("T", " ")).append("\n\n");

        // 汇总
        md.append("## 用量汇总\n\n");
        md.append("| 指标 | 数值 |\n");
        md.append("|------|------|\n");
        md.append("| 请求总数 | ").append(totalRequests).append(" |\n");
        md.append("| 输入 Tokens | ").append(totalPrompt).append(" |\n");
        md.append("| 输出 Tokens | ").append(totalCompletion).append(" |\n");
        md.append("| 总 Tokens | ").append(totalTokens).append(" |\n\n");

        md.append("---\n\n");

        // 明细
        md.append("## 用量明细\n\n");
        md.append("| 日期 | 用户 | 部门 | 模型 | 请求数 | 输入 | 输出 | 总 Tokens |\n");
        md.append("|------|------|------|------|--------|------|------|--------|\n");

        for (ChatTokenUsageEntity usage : usageList) {
            UsersEntity user = userMap.get(usage.getUserId());
            ModelConfigEntity model = modelMap.get(usage.getModelConfigId());
            DeptEntity dept = user != null && user.getDeptId() != null
                    ? deptInfra.getDeptById(user.getDeptId()) : null;

            String userName = user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "未知";
            String deptName = dept != null ? dept.getDeptName() : "-";
            String modelName = model != null ? model.getName() : "-";

            md.append("| ").append(usage.getUsageDate())
                    .append(" | ").append(userName)
                    .append(" | ").append(deptName)
                    .append(" | ").append(modelName)
                    .append(" | ").append(usage.getRequestCount())
                    .append(" | ").append(usage.getPromptTokens())
                    .append(" | ").append(usage.getCompletionTokens())
                    .append(" | ").append(usage.getTotalTokens())
                    .append(" |\n");
        }

        String filename = "token_usage_" + java.time.LocalDateTime.now().toString().substring(0, 10) + ".md";
        return new ExportResult(filename, md.toString());
    }
}
