package org.evolve.domain.resource.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.evolve.domain.resource.infra.ToolGuardConfigInfra;
import org.evolve.domain.resource.infra.ToolGuardHistoryInfra;
import org.evolve.domain.resource.infra.ToolGuardRuleInfra;
import org.evolve.domain.resource.model.ToolGuardConfigEntity;
import org.evolve.domain.resource.model.ToolGuardHistoryEntity;
import org.evolve.domain.resource.model.ToolGuardRuleEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 工具守卫规则引擎
 * <p>
 * 在工具执行前进行安全检查，通过正则匹配工具参数，阻止危险操作。
 * </p>
 */
@Slf4j
@Component
public class ToolGuardEngine {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    private ToolGuardRuleInfra toolGuardRuleInfra;

    @Resource
    private ToolGuardConfigInfra toolGuardConfigInfra;

    @Resource
    private ToolGuardHistoryInfra toolGuardHistoryInfra;

    /**
     * 威胁严重级别
     */
    public enum Severity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW,
        INFO,
        SAFE
    }

    /**
     * 守卫结果
     */
    public static class GuardResult {
        private final boolean blocked;
        private final boolean warning;
        private final String message;
        private final Severity maxSeverity;
        private final ToolGuardRuleEntity matchedRule;
        private final String paramName;
        private final String matchedValue;

        private GuardResult(boolean blocked, boolean warning, String message, Severity maxSeverity,
                           ToolGuardRuleEntity matchedRule, String paramName, String matchedValue) {
            this.blocked = blocked;
            this.warning = warning;
            this.message = message;
            this.maxSeverity = maxSeverity;
            this.matchedRule = matchedRule;
            this.paramName = paramName;
            this.matchedValue = matchedValue;
        }

        public static GuardResult safe() {
            return new GuardResult(false, false, null, Severity.SAFE, null, null, null);
        }

        public static GuardResult blocked(ToolGuardRuleEntity rule, String paramName, String matchedValue) {
            String message = rule.getName() + "：" + rule.getDescription();
            return new GuardResult(true, false, message, Severity.valueOf(rule.getSeverity()), rule, paramName, matchedValue);
        }

        public static GuardResult warning(ToolGuardRuleEntity rule, String paramName, String matchedValue) {
            String message = rule.getName() + "：" + rule.getDescription();
            return new GuardResult(false, true, message, Severity.valueOf(rule.getSeverity()), rule, paramName, matchedValue);
        }

        public boolean isBlocked() { return blocked; }
        public boolean isWarning() { return warning; }
        public boolean isSafe() { return !blocked && !warning; }
        public String getMessage() { return message; }
        public Severity getMaxSeverity() { return maxSeverity; }
        public ToolGuardRuleEntity getMatchedRule() { return matchedRule; }
        public String getParamName() { return paramName; }
        public String getMatchedValue() { return matchedValue; }
    }

    /**
     * 检查工具调用是否安全
     *
     * @param toolName 工具名称
     * @param params 工具参数
     * @return 守卫结果
     */
    public GuardResult guard(String toolName, Map<String, Object> params) {
        // 1. 检查全局开关
        ToolGuardConfigEntity config = toolGuardConfigInfra.getConfig();
        if (config == null || config.getEnabled() != 1) {
            return GuardResult.safe();
        }

        // 2. 检查是否直接拒绝的工具
        if (isDeniedTool(toolName, config.getDeniedTools())) {
            return GuardResult.safe(); // denied_tools是直接放行，仅记录日志
        }

        // 3. 获取适用的规则
        List<ToolGuardRuleEntity> rules = toolGuardRuleInfra.getEnabledRules();

        // 4. 遍历规则进行匹配
        for (ToolGuardRuleEntity rule : rules) {
            if (!isToolApplicable(toolName, rule.getTools())) {
                continue;
            }

            List<String> paramNames = parseJson(rule.getParams(), new TypeReference<>() {});
            for (String paramName : paramNames) {
                Object paramValue = params.get(paramName);
                if (paramValue == null) continue;

                String text = paramValue.toString();
                List<String> patterns = parseJson(rule.getPatterns(), new TypeReference<>() {});

                for (String pattern : patterns) {
                    if (matches(pattern, text) && !matchesAnyExclude(text, rule.getExcludePatterns())) {
                        String snippet = extractSnippet(text, pattern);
                        if (isBlockingSeverity(rule.getSeverity())) {
                            return GuardResult.blocked(rule, paramName, snippet);
                        } else {
                            return GuardResult.warning(rule, paramName, snippet);
                        }
                    }
                }
            }
        }

        return GuardResult.safe();
    }

    /**
     * 记录阻断历史
     */
    public void recordBlocked(String sessionId, Long userId, String userNickname,
                              String toolName, String paramName, String matchedRuleId,
                              String matchedValue, String severity) {
        ToolGuardHistoryEntity history = new ToolGuardHistoryEntity();
        history.setSessionId(sessionId);
        history.setUserId(userId);
        history.setUserNickname(userNickname);
        history.setToolName(toolName);
        history.setParamName(paramName);
        history.setMatchedRuleId(matchedRuleId);
        history.setMatchedValue(matchedValue);
        history.setSeverity(severity);
        history.setAction("BLOCKED");
        history.setCreateTime(LocalDateTime.now());
        toolGuardHistoryInfra.saveHistory(history);
    }

    /**
     * 记录警告历史
     */
    public void recordWarning(String sessionId, Long userId, String userNickname,
                             String toolName, String paramName, String matchedRuleId,
                             String matchedValue, String severity) {
        ToolGuardHistoryEntity history = new ToolGuardHistoryEntity();
        history.setSessionId(sessionId);
        history.setUserId(userId);
        history.setUserNickname(userNickname);
        history.setToolName(toolName);
        history.setParamName(paramName);
        history.setMatchedRuleId(matchedRuleId);
        history.setMatchedValue(matchedValue);
        history.setSeverity(severity);
        history.setAction("WARNED");
        history.setCreateTime(LocalDateTime.now());
        toolGuardHistoryInfra.saveHistory(history);
    }

    private boolean isDeniedTool(String toolName, String deniedToolsJson) {
        if (deniedToolsJson == null || deniedToolsJson.isEmpty() || "[]".equals(deniedToolsJson)) {
            return false;
        }
        try {
            List<String> deniedTools = parseJson(deniedToolsJson, new TypeReference<>() {});
            return deniedTools.contains(toolName);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isToolApplicable(String toolName, String toolsJson) {
        if (toolsJson == null || toolsJson.isEmpty()) {
            return true; // 空表示全部工具
        }
        try {
            List<String> tools = parseJson(toolsJson, new TypeReference<>() {});
            return tools.contains(toolName);
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isBlockingSeverity(String severity) {
        return "CRITICAL".equals(severity) || "HIGH".equals(severity);
    }

    private boolean matches(String pattern, String text) {
        try {
            return Pattern.compile(pattern).matcher(text).find();
        } catch (Exception e) {
            log.warn("Invalid regex pattern: {}", pattern, e);
            return false;
        }
    }

    private boolean matchesAnyExclude(String text, String excludePatternsJson) {
        if (excludePatternsJson == null || excludePatternsJson.isEmpty()) {
            return false;
        }
        try {
            List<String> excludePatterns = parseJson(excludePatternsJson, new TypeReference<>() {});
            for (String pattern : excludePatterns) {
                if (Pattern.compile(pattern).matcher(text).find()) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private String extractSnippet(String text, String pattern) {
        try {
            var matcher = Pattern.compile(pattern).matcher(text);
            if (matcher.find()) {
                String matched = matcher.group();
                int start = Math.max(0, matcher.start() - 10);
                int end = Math.min(text.length(), matcher.end() + 10);
                String snippet = text.substring(start, end);
                if (start > 0) snippet = "..." + snippet;
                if (end < text.length()) snippet = snippet + "...";
                return snippet;
            }
        } catch (Exception ignored) {
        }
        return text.length() > 50 ? text.substring(0, 50) + "..." : text;
    }

    private <T> T parseJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", json, e);
            throw new RuntimeException("JSON解析失败: " + json, e);
        }
    }
}
