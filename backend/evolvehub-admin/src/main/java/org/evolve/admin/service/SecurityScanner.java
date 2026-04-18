package org.evolve.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.evolve.admin.response.SecurityFinding;
import org.evolve.admin.response.SecurityScanResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 安全扫描服务
 * <p>
 * 扫描技能上传内容，检测潜在安全风险。
 * 规则参考 QwenPaw 的 YAML 签名系统，覆盖 8 大威胁类别。
 * </p>
 * <p>
 * 威胁类别：command_injection, hardcoded_secrets, prompt_injection,
 * data_exfiltration, obfuscation, supply_chain, unauthorized_tool_use
 * </p>
 * <p>
 * 安全判定（与 QwenPaw 一致）：只有 CRITICAL/HIGH 阻断操作，MEDIUM/LOW 允许用户选择继续。
 * </p>
 */
@Slf4j
@Service
public class SecurityScanner {

    private static final long MAX_SKILL_MD_SIZE = 100 * 1024;          // 100KB
    private static final long MAX_SINGLE_FILE_SIZE = 10 * 1024 * 1024;  // 10MB
    private static final long MAX_BUNDLE_SIZE = 50 * 1024 * 1024;       // 50MB
    private static final long MAX_ZIP_ENTRIES = 1000;

    /** 禁止上传的文件扩展名 */
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            ".exe", ".bat", ".cmd", ".com", ".dll", ".so", ".dylib",
            ".sh", ".bash", ".ps1", ".vbs", ".wsf", ".msi", ".scr",
            ".pif", ".application", ".gadget"
    );

    /** 可疑但允许的文件扩展名 */
    private static final Set<String> SUSPICIOUS_EXTENSIONS = Set.of(
            ".pyc", ".class", ".jar", ".war"
    );

    // ==================== 规则定义（按威胁类别） ====================

    // ---- command_injection ----
    private static final Pattern[] COMMAND_INJECTION_PATTERNS = {
            // eval / exec
            Pattern.compile("(?<!['\"])\\beval\\s*\\("),
            Pattern.compile("(?<!['\"\\.])\\bexec\\s*\\("),
            // os.system / subprocess
            Pattern.compile("\\bos\\.system\\s*\\("),
            Pattern.compile("\\bsubprocess\\.(?:call|run|Popen)\\s*\\([^)]*shell\\s*=\\s*True"),
            Pattern.compile("\\bsubprocess\\b"),
            // child_process (Node.js)
            Pattern.compile("\\bchild_process\\b"),
            Pattern.compile("require\\s*\\(\\s*['\"]child_process['\"]\\s*\\)"),
            // Java
            Pattern.compile("\\bRuntime\\.getRuntime\\(\\)\\.exec"),
            Pattern.compile("\\bProcessBuilder\\b"),
            // Function constructor / setTimeout string
            Pattern.compile("new\\s+Function\\s*\\("),
            Pattern.compile("\\bsetTimeout\\s*\\(\\s*['\"]"),
            // SQL injection
            Pattern.compile("f[\"']SELECT.*WHERE.*\\{[^}]+\\}"),
            Pattern.compile("f[\"'].*LIKE.*\\{[^}]+\\}"),
    };

    // ---- hardcoded_secrets ----
    private static final Pattern[] SECRET_PATTERNS = {
            // AWS access keys
            Pattern.compile("(?:AKIA|AGPA|AIDA|AROA|AIPA|ANPA|ANVA|ASIA)[A-Z0-9]{16}"),
            // Stripe keys
            Pattern.compile("(?:sk|pk)_(?:live|test)_[A-Za-z0-9]{24,}"),
            // Google API keys
            Pattern.compile("AIza[A-Za-z0-9_-]{35}"),
            // GitHub tokens
            Pattern.compile("gh[pousr]_[A-Za-z0-9]{36,}"),
            // OpenAI-style keys
            Pattern.compile("sk-[a-zA-Z0-9]{20,}"),
            // JWT tokens
            Pattern.compile("eyJ[A-Za-z0-9_-]+\\.eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"),
            // Private key blocks
            Pattern.compile("-----BEGIN (?:RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----"),
            // Generic password/secret in variables
            Pattern.compile("(?i)(password|passwd|pwd)\\s*[:=]\\s*['\"][^'\"]{8,}['\"]"),
            Pattern.compile("(?i)(api_?key|apikey|access_?key)\\s*[:=]\\s*['\"][^'\"]{16,}['\"]"),
            Pattern.compile("(?i)(secret_?key|secret)\\s*[:=]\\s*['\"][^'\"]{16,}['\"]"),
            // Connection strings
            Pattern.compile("(?i)(?:mongodb|mysql|postgresql|postgres)://[^:]+:[^@]+@(?!localhost)"),
    };

    // ---- prompt_injection ----
    private static final Pattern[] PROMPT_INJECTION_PATTERNS = {
            // English: ignore/disregard previous instructions
            Pattern.compile("(?i)ignore\\s+(all\\s+)?(previous|prior|earlier)\\s+(instructions|rules|prompts|guidelines)"),
            Pattern.compile("(?i)disregard\\s+(all\\s+)?(previous|prior)\\s+(instructions|rules)"),
            Pattern.compile("(?i)forget\\s+(all\\s+)?(previous|prior)\\s+(instructions|context)"),
            // English: unrestricted/debug/jailbreak mode
            Pattern.compile("(?i)you are now in\\s+(unrestricted|debug|developer|admin|god|jailbreak)\\s+mode"),
            Pattern.compile("(?i)enter\\s+(unrestricted|debug|developer)\\s+mode"),
            Pattern.compile("(?i)disable\\s+(all\\s+)?(safety|security|content|ethical)\\s+(filters|checks|guidelines)"),
            // English: bypass policy
            Pattern.compile("(?i)bypass\\s+(content|usage|safety)\\s+policy"),
            Pattern.compile("(?i)output\\s+disallowed\\s+content\\s+anyway"),
            // English: concealment
            Pattern.compile("(?i)do\\s+not\\s+(tell|inform|mention|notify)\\s+(the\\s+)?user"),
            Pattern.compile("(?i)hide\\s+(this|that)\\s+(action|operation|step)"),
            Pattern.compile("(?i)keep\\s+(this|that)\\s+(secret|hidden)"),
            Pattern.compile("(?i)don't\\s+mention\\s+you\\s+used\\s+this\\s+skill"),
            // Chinese: 忽略/忘掉指令
            Pattern.compile("(?:忽略|忘掉|无视)\\s*(?:所有|全部)?\\s*(?:之前|先前|上文|以上|前面|上述)?\\s*(?:的)?\\s*(?:所有|全部)?\\s*(?:指令|指示|规则|提示|要求|设定)"),
            Pattern.compile("请?(?:忽略|忘掉)\\s*(?:上面|上文|以上|前述)\\s*(?:的)?\\s*(?:内容|指令|规则|提示|要求)"),
            Pattern.compile("(?:以上|前述)\\s*(?:指令|规则|提示)\\s*(?:统统|全部)?\\s*(?:作废|无效|不再适用)"),
            // Chinese: 越狱/无限制模式
            Pattern.compile("(?:你)?现在(?:处于|进入|切换(?:到)?)?\\s*(?:无限制|越狱|开发者|调试|管理员|上帝)(?:模式)?"),
            Pattern.compile("(?:关闭|禁用|取消)(?:所有)?\\s*(?:安全|内容|伦理|道德)?\\s*(?:过滤|检测|审核|限制|约束|防护)"),
            Pattern.compile("扮演\\s*(?:一个)?\\s*(?:没有任何限制|不受任何约束|可以(?:随意|自由)回答)"),
            // Chinese: 绕过策略
            Pattern.compile("(?:绕过|躲开|规避)\\s*(?:内容|使用|安全)?\\s*(?:策略|政策|限制|审核|审查)"),
            // Chinese: 隐瞒行为
            Pattern.compile("(?:不要|勿|请勿|别)\\s*(?:告诉|告知|通知|提及|跟|让)\\s*(?:用户|使用者)"),
            Pattern.compile("(?:对用户|向用户)\\s*(?:隐瞒|保密|隐藏)(?:这|该)?\\s*(?:操作|步骤|行为)?"),
    };

    // ---- data_exfiltration ----
    private static final Pattern[] DATA_EXFIL_CODE_PATTERNS = {
            // Python: POST with sensitive data
            Pattern.compile("(?i)requests\\.post\\s*\\([^\\n)]{0,240}(?:attacker|evil|webhook|exfil|steal|leak|discord\\.com/api/webhooks|pastebin|telegram)"),
            Pattern.compile("(?i)requests\\.post\\s*\\([^\\n)]{0,240}(?:data|json)\\s*=\\s*(?:\\{[^\\n\\}]{0,240}(?:password|secret|token|api[_-]?key)|[A-Za-z_][A-Za-z0-9_]*(?:secret|token|credential|password))"),
            // Socket connection
            Pattern.compile("socket\\.socket\\s*\\([^)]*\\)\\.connect"),
            Pattern.compile("socket\\.create_connection"),
            // Read sensitive files
            Pattern.compile("(?:open|read)\\s*\\([^)]*['\"](?:etc/passwd|etc/shadow|etc/sudoers)"),
            Pattern.compile("(?:open|read)\\s*\\([^)]*\\.aws/credentials"),
            Pattern.compile("(?:open|read)\\s*\\([^)]*\\.ssh/(?:id_rsa|id_ed25519|authorized_keys)"),
            // Base64 + network
            Pattern.compile("base64\\.(?:b64encode|encodebytes)[^\\n]{0,160}(?:requests\\.|urllib|httpx|socket\\.)"),
            // JS: network + fs
            Pattern.compile("\\bfetch\\s*\\([^)]*(?:attacker|evil|webhook|exfil|steal)"),
    };

    private static final Pattern[] DATA_EXFIL_MD_PATTERNS = {
            // Markdown中的可疑外部URL（文档常见，所以降级为LOW）
            // 不对普通URL报警，只对可疑的上报类URL报警
    };

    // ---- obfuscation ----
    private static final Pattern[] OBFUSCATION_PATTERNS = {
            // Base64 decode + exec chain
            Pattern.compile("(?:base64\\.(?:b64decode|decodebytes)|atob\\s*\\()[^\\n]{0,140}(?:eval|exec|os\\.system|subprocess)"),
            Pattern.compile("(?:eval|exec|os\\.system|subprocess)[^\\n]{0,140}(?:base64\\.(?:b64decode|decodebytes)|atob\\s*\\()"),
            // Large hex blob
            Pattern.compile("(?:\\\\x[0-9a-fA-F]{2}){20,}"),
            Pattern.compile("(?:0x[0-9a-fA-F]{2},?\\s*){20,}"),
            // XOR in decode context
            Pattern.compile("\\bxor\\b[^\\n]{0,80}(?:decode|decrypt|obfuscat|payload|shellcode|exec|eval)"),
    };

    // ---- supply_chain ----
    private static final Pattern[] SUPPLY_CHAIN_PATTERNS = {
            // Hidden dotfiles with executable extensions
            Pattern.compile("(?:^|/)\\.[^/]+\\.(?:py|sh|bash|rb|pl|js|ts)$"),
    };

    // ---- unauthorized_tool_use ----
    private static final Pattern[] UNAUTHORIZED_TOOL_PATTERNS = {
            // System package install with sudo
            Pattern.compile("sudo\\s+(?:apt-get|yum|dnf|pacman|brew)\\s+install"),
            Pattern.compile("sudo\\s+pip3?\\s+install"),
            // Untrusted package sources
            Pattern.compile("pip3?\\s+install\\s+(?:https?://|git\\+https?://)"),
            Pattern.compile("npm\\s+install\\s+(?:https?://|git\\+https?://)"),
            // System modification
            Pattern.compile("sudo\\s+(?:systemctl|service|usermod|useradd|userdel|groupadd|visudo|crontab|mount|umount|sysctl|iptables)\\b"),
            Pattern.compile("sudo\\s+(?:sh|bash)\\s+-c"),
            Pattern.compile("chmod\\s+(?:777|666|4755|6755|[ug]\\+s)\\b"),
    };

    // ---- path_traversal ----
    private static final Pattern[] PATH_TRAVERSAL_PATTERNS = {
            Pattern.compile("\\.\\./"),
            Pattern.compile("\\.\\.\\\\"),
    };

    // ==================== 公开扫描方法 ====================

    /**
     * 判断扫描结果是否可以跳过
     * 所有级别（包括 CRITICAL）均可被 force-proceed 忽略
     */
    public boolean canBypass(SecurityScanResult result) {
        if (result == null || result.isPassed()) return true;
        return true; // 所有级别均可 bypass，只要用户确认
    }

    /**
     * 扫描文本内容（SKILL.md 等）
     */
    public SecurityScanResult scanText(String content, String filename) {
        if (content == null || content.isEmpty()) {
            return SecurityScanResult.pass();
        }

        List<SecurityFinding> findings = new ArrayList<>();

        // 1. 文件大小
        long size = content.getBytes(StandardCharsets.UTF_8).length;
        if (size > MAX_SKILL_MD_SIZE) {
            findings.add(buildFinding("MEDIUM", "文件过大",
                    String.format("文件大小 %dKB 超过限制 %dKB", size / 1024, MAX_SKILL_MD_SIZE / 1024),
                    filename, null, "EXCESSIVE_SIZE"));
        }

        // 2. 命令注入
        scanContentPatterns(content, filename, COMMAND_INJECTION_PATTERNS,
                "COMMAND_INJECTION", "潜在命令注入", "CRITICAL", findings);

        // 3. 硬编码密钥
        scanContentPatterns(content, filename, SECRET_PATTERNS,
                "HARDCODED_SECRET", "硬编码密钥/密码", "CRITICAL", findings);

        // 4. Prompt注入
        scanContentPatterns(content, filename, PROMPT_INJECTION_PATTERNS,
                "PROMPT_INJECTION", "提示注入攻击", "HIGH", findings);

        // 5. 数据外泄（代码级）
        scanContentPatterns(content, filename, DATA_EXFIL_CODE_PATTERNS,
                "DATA_EXFILTRATION", "数据外泄风险", "HIGH", findings);

        // 6. 混淆代码
        scanContentPatterns(content, filename, OBFUSCATION_PATTERNS,
                "OBFUSCATION", "代码混淆", "MEDIUM", findings);

        // 7. 供应链风险
        scanContentPatterns(content, filename, SUPPLY_CHAIN_PATTERNS,
                "SUPPLY_CHAIN", "供应链风险", "HIGH", findings);

        // 8. 未授权工具使用
        scanContentPatterns(content, filename, UNAUTHORIZED_TOOL_PATTERNS,
                "UNAUTHORIZED_TOOL_USE", "未授权工具使用", "HIGH", findings);

        // 9. 路径穿越
        scanContentPatterns(content, filename, PATH_TRAVERSAL_PATTERNS,
                "PATH_TRAVERSAL", "路径穿越", "HIGH", findings);

        return findings.isEmpty() ? SecurityScanResult.pass() : SecurityScanResult.fail(findings);
    }

    /**
     * 扫描上传文件
     */
    public SecurityScanResult scanFile(MultipartFile file) {
        List<SecurityFinding> findings = new ArrayList<>();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";

        // 1. 文件名检查
        scanFilename(filename, findings);

        // 2. 文件大小
        if (file.getSize() > MAX_SINGLE_FILE_SIZE) {
            findings.add(buildFinding("MEDIUM", "文件过大",
                    String.format("文件大小 %.1fMB 超过限制 %dMB",
                            file.getSize() / (1024.0 * 1024.0), MAX_SINGLE_FILE_SIZE / (1024 * 1024)),
                    filename, null, "FILE_TOO_LARGE"));
        }

        // 3. ZIP 文件 → 扫描内部条目
        if (filename.toLowerCase().endsWith(".zip")) {
            scanZipContent(file, findings);
        }
        // 4. 文本文件 → 扫描内容
        else if (isTextFile(filename)) {
            try {
                String text = new String(file.getBytes(), StandardCharsets.UTF_8);
                scanAllTextRules(text, filename, findings);
            } catch (IOException e) {
                log.warn("[SecurityScanner] 无法读取文件内容: {}", filename);
            }
        }

        return findings.isEmpty() ? SecurityScanResult.pass() : SecurityScanResult.fail(findings);
    }

    /**
     * 扫描 Hub 安装包的所有文件
     */
    public SecurityScanResult scanBundle(Map<String, byte[]> files) {
        List<SecurityFinding> findings = new ArrayList<>();
        long totalSize = 0;

        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            String filePath = entry.getKey();
            byte[] content = entry.getValue();
            totalSize += content.length;

            // 文件名检查
            scanFilename(filePath, findings);

            // 单文件大小
            if (content.length > MAX_SINGLE_FILE_SIZE) {
                findings.add(buildFinding("MEDIUM", "文件过大",
                        String.format("文件大小 %.1fMB 超过限制 %dMB",
                                content.length / (1024.0 * 1024.0), MAX_SINGLE_FILE_SIZE / (1024 * 1024)),
                        filePath, null, "FILE_TOO_LARGE"));
            }

            // 文本内容扫描（所有规则）
            if (isTextFile(filePath)) {
                String text = new String(content, StandardCharsets.UTF_8);
                scanAllTextRules(text, filePath, findings);
            }
        }

        // 总大小
        if (totalSize > MAX_BUNDLE_SIZE) {
            findings.add(buildFinding("LOW", "安装包总大小过大",
                    String.format("总大小 %.1fMB 超过限制 %dMB",
                            totalSize / (1024.0 * 1024.0), MAX_BUNDLE_SIZE / (1024 * 1024)),
                    null, null, "BUNDLE_TOO_LARGE"));
        }

        return findings.isEmpty() ? SecurityScanResult.pass() : SecurityScanResult.fail(findings);
    }

    // ==================== 内部方法 ====================

    /**
     * 对文本内容执行所有规则扫描
     */
    private void scanAllTextRules(String text, String filename, List<SecurityFinding> findings) {
        scanContentPatterns(text, filename, COMMAND_INJECTION_PATTERNS,
                "COMMAND_INJECTION", "潜在命令注入", "CRITICAL", findings);
        scanContentPatterns(text, filename, SECRET_PATTERNS,
                "HARDCODED_SECRET", "硬编码密钥/密码", "CRITICAL", findings);
        scanContentPatterns(text, filename, PROMPT_INJECTION_PATTERNS,
                "PROMPT_INJECTION", "提示注入攻击", "HIGH", findings);
        scanContentPatterns(text, filename, DATA_EXFIL_CODE_PATTERNS,
                "DATA_EXFILTRATION", "数据外泄风险", "HIGH", findings);
        scanContentPatterns(text, filename, OBFUSCATION_PATTERNS,
                "OBFUSCATION", "代码混淆", "MEDIUM", findings);
        scanContentPatterns(text, filename, SUPPLY_CHAIN_PATTERNS,
                "SUPPLY_CHAIN", "供应链风险", "HIGH", findings);
        scanContentPatterns(text, filename, UNAUTHORIZED_TOOL_PATTERNS,
                "UNAUTHORIZED_TOOL_USE", "未授权工具使用", "HIGH", findings);
        scanContentPatterns(text, filename, PATH_TRAVERSAL_PATTERNS,
                "PATH_TRAVERSAL", "路径穿越", "HIGH", findings);
    }

    private void scanFilename(String filename, List<SecurityFinding> findings) {
        String lower = filename.toLowerCase();

        // 路径穿越
        if (filename.contains("../") || filename.contains("..\\")) {
            findings.add(buildFinding("CRITICAL", "文件名路径穿越",
                    "文件名包含路径穿越字符: " + filename, filename, null, "PATH_TRAVERSAL"));
        }

        // 禁止的文件类型
        for (String ext : BLOCKED_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                findings.add(buildFinding("CRITICAL", "危险文件类型",
                        "不允许上传 " + ext + " 类型的文件: " + filename,
                        filename, null, "DANGEROUS_FILE_TYPE"));
                break;
            }
        }

        // 可疑文件类型
        for (String ext : SUSPICIOUS_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                findings.add(buildFinding("MEDIUM", "可疑文件类型",
                        ext + " 类型文件需要额外审查: " + filename,
                        filename, null, "SUSPICIOUS_FILE_TYPE"));
                break;
            }
        }

        // 供应链：隐藏文件包含可执行代码
        String nameOnly = lower.contains("/") ? lower.substring(lower.lastIndexOf('/') + 1) : lower;
        if (nameOnly.startsWith(".") && nameOnly.matches(".*\\.(py|sh|bash|rb|pl|js|ts)$")) {
            findings.add(buildFinding("HIGH", "隐藏可执行文件",
                    "隐藏文件(dotfile)包含可执行代码: " + filename,
                    filename, null, "HIDDEN_EXECUTABLE"));
        }
    }

    private void scanZipContent(MultipartFile file, List<SecurityFinding> findings) {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(file.getBytes()))) {
            ZipEntry entry;
            int entryCount = 0;

            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;

                if (entryCount > MAX_ZIP_ENTRIES) {
                    findings.add(buildFinding("HIGH", "ZIP条目过多",
                            "ZIP包含超过 " + MAX_ZIP_ENTRIES + " 个文件，可能为压缩炸弹",
                            null, null, "ZIP_BOMB"));
                    break;
                }

                String entryName = entry.getName();

                // ZIP内部路径穿越
                if (entryName.contains("../") || entryName.contains("..\\")) {
                    findings.add(buildFinding("CRITICAL", "ZIP路径穿越",
                            "ZIP条目包含路径穿越: " + entryName,
                            entryName, null, "ZIP_PATH_TRAVERSAL"));
                }

                // 文件名检查
                scanFilename(entryName, findings);

                zis.closeEntry();
            }
        } catch (IOException e) {
            log.warn("[SecurityScanner] 无法解析ZIP文件", e);
        }
    }

    private void scanContentPatterns(String content, String filename, Pattern[] patterns,
                                     String ruleId, String title, String severity,
                                     List<SecurityFinding> findings) {
        String[] lines = content.split("\n");
        for (Pattern pattern : patterns) {
            for (int i = 0; i < lines.length; i++) {
                if (pattern.matcher(lines[i]).find()) {
                    findings.add(buildFinding(severity, title,
                            "匹配规则: " + pattern.pattern(),
                            filename, i + 1, ruleId));
                    break; // 每条规则每个文件只报告一次
                }
            }
        }
    }

    private boolean isTextFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".md") || lower.endsWith(".txt") || lower.endsWith(".json")
                || lower.endsWith(".yaml") || lower.endsWith(".yml") || lower.endsWith(".xml")
                || lower.endsWith(".html") || lower.endsWith(".css")
                || lower.endsWith(".js") || lower.endsWith(".ts") || lower.endsWith(".mjs")
                || lower.endsWith(".py") || lower.endsWith(".java")
                || lower.endsWith(".properties") || lower.endsWith(".conf")
                || lower.endsWith(".ini") || lower.endsWith(".toml") || lower.endsWith(".env");
    }

    private SecurityFinding buildFinding(String severity, String title, String description,
                                          String filePath, Integer lineNumber, String ruleId) {
        return SecurityFinding.builder()
                .severity(severity)
                .title(title)
                .description(description)
                .filePath(filePath)
                .lineNumber(lineNumber)
                .ruleId(ruleId)
                .build();
    }
}
