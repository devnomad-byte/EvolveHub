package org.evolve.domain.resource.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.evolve.domain.resource.infra.FileGuardConfigInfra;
import org.evolve.domain.resource.infra.FileGuardRuleInfra;
import org.evolve.domain.resource.model.FileGuardConfigEntity;
import org.evolve.domain.resource.model.FileGuardRuleEntity;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 文件守卫规则引擎
 * <p>
 * 在工具执行前检查文件路径是否指向敏感资源。
 * </p>
 */
@Slf4j
@Component
public class FileGuardEngine {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 工具名 -> 需要检查的参数名列表 */
    private static final Map<String, String[]> TOOL_FILE_PARAMS = Map.ofEntries(
            Map.entry("read_file", new String[]{"file_path"}),
            Map.entry("write_file", new String[]{"file_path"}),
            Map.entry("edit_file", new String[]{"file_path"}),
            Map.entry("append_file", new String[]{"file_path"}),
            Map.entry("send_file_to_user", new String[]{"file_path"}),
            Map.entry("view_text_file", new String[]{"file_path", "path"}),
            Map.entry("write_text_file", new String[]{"file_path", "path"})
    );

    @Resource
    private FileGuardRuleInfra fileGuardRuleInfra;

    @Resource
    private FileGuardConfigInfra fileGuardConfigInfra;

    /**
     * 守卫结果
     */
    public static class GuardResult {
        private final boolean blocked;
        private final boolean warning;
        private final String message;
        private final String severity;
        private final FileGuardRuleEntity matchedRule;
        private final String paramName;
        private final String matchedPath;

        private GuardResult(boolean blocked, boolean warning, String message, String severity,
                           FileGuardRuleEntity matchedRule, String paramName, String matchedPath) {
            this.blocked = blocked;
            this.warning = warning;
            this.message = message;
            this.severity = severity;
            this.matchedRule = matchedRule;
            this.paramName = paramName;
            this.matchedPath = matchedPath;
        }

        public static GuardResult safe() {
            return new GuardResult(false, false, null, null, null, null, null);
        }

        public static GuardResult blocked(FileGuardRuleEntity rule, String paramName, String matchedPath) {
            String message = rule.getName() + "：" + rule.getDescription();
            return new GuardResult(true, false, message, rule.getSeverity(), rule, paramName, matchedPath);
        }

        public static GuardResult warning(FileGuardRuleEntity rule, String paramName, String matchedPath) {
            String message = rule.getName() + "：" + rule.getDescription();
            return new GuardResult(false, true, message, rule.getSeverity(), rule, paramName, matchedPath);
        }

        public boolean isBlocked() { return blocked; }
        public boolean isWarning() { return warning; }
        public boolean isSafe() { return !blocked && !warning; }
        public String getMessage() { return message; }
        public String getSeverity() { return severity; }
        public FileGuardRuleEntity getMatchedRule() { return matchedRule; }
        public String getParamName() { return paramName; }
        public String getMatchedPath() { return matchedPath; }
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
        FileGuardConfigEntity config = fileGuardConfigInfra.getConfig();
        if (config == null || config.getEnabled() != 1) {
            return GuardResult.safe();
        }

        // 2. 获取适用的规则
        List<FileGuardRuleEntity> rules = fileGuardRuleInfra.getEnabledRules();
        if (rules.isEmpty()) {
            return GuardResult.safe();
        }

        // 3. 获取该工具需要检查的参数
        String[] paramsToCheck = getParamsToCheck(toolName);

        // 4. 遍历参数进行检查
        for (String paramName : paramsToCheck) {
            Object paramValue = params.get(paramName);
            if (paramValue == null) continue;

            String pathValue = paramValue.toString();

            // 从路径值中提取文件路径（如果是shell命令，需要提取重定向路径）
            List<String> pathsToCheck = extractPaths(toolName, paramName, pathValue);

            for (String path : pathsToCheck) {
                String normalizedPath = normalizePath(path);

                // 5. 与规则进行匹配
                for (FileGuardRuleEntity rule : rules) {
                    if (!isToolApplicable(toolName, rule.getTools())) {
                        continue;
                    }

                    if (matchesPath(rule, normalizedPath)) {
                        if (isBlockingSeverity(rule.getSeverity())) {
                            return GuardResult.blocked(rule, paramName, normalizedPath);
                        } else {
                            return GuardResult.warning(rule, paramName, normalizedPath);
                        }
                    }
                }
            }
        }

        return GuardResult.safe();
    }

    /**
     * 获取工具需要检查的参数名
     */
    private String[] getParamsToCheck(String toolName) {
        // 如果是 shell 命令，检查 command 参数中的路径
        if ("execute_shell_command".equals(toolName)) {
            return new String[]{"command"};
        }
        return TOOL_FILE_PARAMS.getOrDefault(toolName, new String[]{});
    }

    /**
     * 从参数值中提取文件路径
     */
    private List<String> extractPaths(String toolName, String paramName, String value) {
        // shell 命令需要解析重定向操作符
        if ("command".equals(paramName)) {
            return extractPathsFromShellCommand(value);
        }
        return List.of(value);
    }

    /**
     * 从 shell 命令中提取文件路径（处理 >, >>, 2>, < 等重定向）
     */
    private List<String> extractPathsFromShellCommand(String command) {
        java.util.List<String> paths = new java.util.ArrayList<>();
        if (command == null || command.isEmpty()) {
            return paths;
        }

        // 简单的空格分割
        String[] tokens = command.split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            // 检查重定向操作符
            if (token.contains(">")) {
                String[] parts = token.split(">", 2);
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    String redirectedPath = parts[1];
                    if (looksLikePath(redirectedPath)) {
                        paths.add(redirectedPath);
                    }
                }
                // 检查下一个 token（如果有空格分隔的重定向）
                if (i + 1 < tokens.length && (token.equals(">") || token.equals(">>") || token.equals("2>"))) {
                    String nextToken = tokens[i + 1];
                    if (looksLikePath(nextToken)) {
                        paths.add(nextToken);
                    }
                }
            } else if (token.contains("<")) {
                String[] parts = token.split("<", 2);
                if (parts.length > 1 && !parts[1].isEmpty() && looksLikePath(parts[1])) {
                    paths.add(parts[1]);
                }
            } else if (looksLikePath(token)) {
                // 排除明显的命令名
                if (!isCommandName(token)) {
                    paths.add(token);
                }
            }
        }

        return paths;
    }

    /**
     * 判断是否像路径
     */
    private boolean looksLikePath(String token) {
        if (token == null || token.isEmpty() || token.startsWith("-")) {
            return false;
        }
        String lower = token.toLowerCase();
        // 排除 URL
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("ftp://")) {
            return false;
        }
        // 看起来像路径
        return token.startsWith("/") || token.startsWith("./") || token.startsWith("../")
                || token.startsWith("~") || token.contains(":\\");
    }

    /**
     * 判断是否像命令名（简单判断）
     */
    private boolean isCommandName(String token) {
        // 简单判断，不包含 / 和 .
        return !token.contains("/") && !token.contains(".");
    }

    /**
     * 标准化路径
     */
    private String normalizePath(String path) {
        if (path == null) return "";
        // 展开 ~ 为用户目录
        if (path.startsWith("~")) {
            String userHome = System.getProperty("user.home");
            path = userHome + path.substring(1);
        }
        // 转换为绝对路径（简化处理）
        try {
            Path p = Paths.get(path);
            if (!p.isAbsolute()) {
                // 相对路径假设是当前目录
                p = Paths.get(System.getProperty("user.dir"), path);
            }
            return p.normalize().toString();
        } catch (Exception e) {
            return path;
        }
    }

    /**
     * 判断路径是否匹配规则
     */
    private boolean matchesPath(FileGuardRuleEntity rule, String normalizedPath) {
        String pattern = rule.getPathPattern();
        String pathType = rule.getPathType();

        try {
            switch (pathType) {
                case "DIRECTORY":
                    // 目录规则：检查 normalizedPath 是否在 pattern 目录下
                    return normalizedPath.startsWith(pattern)
                            || normalizedPath.replace("\\", "/").startsWith(pattern.replace("\\", "/"));
                case "FILE":
                    // 文件规则：精确匹配
                    return normalizedPath.equals(pattern)
                            || normalizedPath.replace("\\", "/").equals(pattern.replace("\\", "/"));
                case "WILDCARD":
                    // 通配符规则：使用正则匹配
                    String regex = wildcardToRegex(pattern);
                    return Pattern.compile(regex).matcher(normalizedPath).find()
                            || Pattern.compile(regex).matcher(normalizedPath.replace("\\", "/")).find();
                default:
                    return false;
            }
        } catch (Exception e) {
            log.warn("Path matching error: pattern={}, path={}", pattern, normalizedPath, e);
            return false;
        }
    }

    /**
     * 通配符转为正则表达式
     */
    private String wildcardToRegex(String wildcard) {
        StringBuilder sb = new StringBuilder();
        for (char c : wildcard.toCharArray()) {
            switch (c) {
                case '*':
                    sb.append(".*");
                    break;
                case '?':
                    sb.append(".");
                    break;
                case '.':
                case '\\':
                case '/':
                    sb.append(Pattern.quote(String.valueOf(c)));
                    break;
                default:
                    sb.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return sb.toString();
    }

    /**
     * 判断工具是否适用于规则
     */
    private boolean isToolApplicable(String toolName, String toolsJson) {
        if (toolsJson == null || toolsJson.isEmpty() || "[]".equals(toolsJson)) {
            return true; // 空表示全部工具
        }
        try {
            List<String> tools = MAPPER.readValue(toolsJson, new TypeReference<>() {});
            return tools.contains(toolName);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 判断是否为阻断级别
     */
    private boolean isBlockingSeverity(String severity) {
        return "CRITICAL".equals(severity) || "HIGH".equals(severity);
    }
}
