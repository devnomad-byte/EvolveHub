package org.evolve.admin.service;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;

import jakarta.annotation.Resource;
import tools.jackson.databind.json.JsonMapper;
import org.evolve.admin.utils.FileStorageUtil;
import org.evolve.domain.resource.infra.McpConfigInfra;
import org.evolve.domain.resource.infra.McpInstanceInfra;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.evolve.domain.resource.model.McpInstanceEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cn.hutool.json.JSONUtil;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * MCP 服务实例管理
 *
 * 负责 MCP Server 的启动、停止、状态查询，心跳检测
 * 使用官方 MCP SDK: io.modelcontextprotocol.sdk:mcp
 *
 * @author devnomad-byte
 * @version v1.1
 * @date 2026/4/15
 */
@Service
public class McpInstanceService {

    private static final Logger log = LoggerFactory.getLogger(McpInstanceService.class);

    @Resource
    private McpConfigInfra mcpConfigInfra;

    @Resource
    private McpInstanceInfra mcpInstanceInfra;

    @Resource
    private McpToolDiscoveryService mcpToolDiscoveryService;

    @Resource
    private FileStorageUtil fileStorageUtil;

    /** MCP 实例工作目录基路径 */
    @Value("${mcp.instance.base-dir:./mcp-instances}")
    private String baseDir;

    // MCP 客户端缓存: instanceKey -> McpSyncClient
    private final Map<String, McpSyncClient> mcpClientCache = new ConcurrentHashMap<>();

    /**
     * 启动 MCP Server
     */
    public McpInstanceEntity startMcp(Long mcpConfigId) {
        // 1. 检查是否已有运行中的实例
        McpInstanceEntity existing = mcpInstanceInfra.getRunningByConfigId(mcpConfigId);
        if (existing != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "MCP Server 已在运行中");
        }

        // 2. 获取 MCP 配置
        McpConfigEntity config = mcpConfigInfra.getById(mcpConfigId);
        if (config == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP 配置不存在");
        }

        // 3. 创建实例记录
        String instanceKey = UUID.randomUUID().toString();
        McpInstanceEntity instance = new McpInstanceEntity();
        instance.setMcpConfigId(mcpConfigId);
        instance.setInstanceKey(instanceKey);
        instance.setStatus("STARTING");
        instance.setTransportType(config.getTransportType());
        instance.setStartTime(LocalDateTime.now());
        mcpInstanceInfra.saveInstance(instance);

        try {
            // 4. 根据传输类型创建并连接客户端
            McpSyncClient client = createMcpClient(config, instanceKey);
            mcpClientCache.put(instanceKey, client);

            // 5. 初始化连接
            client.initialize();

            // 6. 更新实例状态
            instance.setStatus("RUNNING");
            instance.setLastHeartbeat(LocalDateTime.now());
            mcpInstanceInfra.updateInstance(instance);

            // 7. 自动发现工具
            mcpToolDiscoveryService.discoverAndSaveTools(mcpConfigId, client);

            log.info("MCP Server 启动成功: configId={}, instanceKey={}", mcpConfigId, instanceKey);
            return instance;

        } catch (Exception e) {
            log.error("MCP Server 启动失败: configId={}", mcpConfigId, e);
            instance.setStatus("ERROR");
            instance.setErrorMsg(e.getMessage());
            mcpInstanceInfra.updateInstance(instance);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "MCP Server 启动失败: " + e.getMessage());
        }
    }

    /**
     * 停止 MCP Server
     */
    public void stopMcp(Long mcpConfigId) {
        McpInstanceEntity instance = mcpInstanceInfra.getRunningByConfigId(mcpConfigId);
        if (instance == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP Server 未运行");
        }

        try {
            // 1. 关闭客户端
            McpSyncClient client = mcpClientCache.remove(instance.getInstanceKey());
            if (client != null) {
                client.closeGracefully();
            }
        } catch (Exception e) {
            log.warn("关闭 MCP 客户端异常（已忽略）: {}", e.getMessage());
        }

        // 2. 无论客户端关闭是否异常，都更新实例状态
        instance.setStatus("STOPPED");
        instance.setStopTime(LocalDateTime.now());
        mcpInstanceInfra.updateInstance(instance);

        log.info("MCP Server 停止成功: configId={}, instanceKey={}", mcpConfigId, instance.getInstanceKey());
    }

    /**
     * 重启 MCP Server
     */
    public McpInstanceEntity restartMcp(Long mcpConfigId) {
        McpInstanceEntity instance = mcpInstanceInfra.getRunningByConfigId(mcpConfigId);
        if (instance == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP Server 未运行");
        }

        String instanceKey = instance.getInstanceKey();

        try {
            // 1. 获取旧客户端并关闭
            McpSyncClient oldClient = mcpClientCache.remove(instanceKey);
            if (oldClient != null) {
                oldClient.closeGracefully();
            }

            // 2. 获取最新配置
            McpConfigEntity config = mcpConfigInfra.getById(mcpConfigId);
            if (config == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "MCP 配置不存在");
            }

            // 3. 创建新客户端并连接
            McpSyncClient newClient = createMcpClient(config, instanceKey);
            mcpClientCache.put(instanceKey, newClient);
            newClient.initialize();

            // 4. 更新实例状态
            instance.setStatus("RUNNING");
            instance.setLastHeartbeat(LocalDateTime.now());
            instance.setErrorMsg(null);
            mcpInstanceInfra.updateInstance(instance);

            // 5. 重新发现工具
            mcpToolDiscoveryService.discoverAndSaveTools(mcpConfigId, newClient);

            log.info("MCP Server 重启成功: configId={}, instanceKey={}", mcpConfigId, instanceKey);
            return instance;

        } catch (Exception e) {
            log.error("MCP Server 重启失败: configId={}", mcpConfigId, e);
            instance.setStatus("ERROR");
            instance.setErrorMsg(e.getMessage());
            mcpInstanceInfra.updateInstance(instance);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "MCP Server 重启失败: " + e.getMessage());
        }
    }

    /**
     * 获取 MCP 运行状态（真正探测连接）
     *
     * 1. 查数据库中 RUNNING 状态的记录
     * 2. 检查内存中是否有对应的 McpSyncClient
     * 3. 如果有 client，尝试 listTools() 探测连接是否真正存活
     * 4. 如果 client 丢失或连接已死，修正数据库状态
     */
    public McpInstanceEntity getStatus(Long mcpConfigId) {
        McpInstanceEntity instance = mcpInstanceInfra.getRunningByConfigId(mcpConfigId);
        if (instance == null) {
            return null;
        }

        // 检查内存中的客户端是否存在
        McpSyncClient client = mcpClientCache.get(instance.getInstanceKey());
        if (client == null) {
            // 内存中没有 client → 进程重启或 client 已丢失
            log.warn("MCP 客户端丢失，修正状态为 STOPPED: configId={}, instanceKey={}",
                    mcpConfigId, instance.getInstanceKey());
            instance.setStatus("STOPPED");
            instance.setStopTime(LocalDateTime.now());
            instance.setErrorMsg("服务重启导致连接丢失");
            mcpInstanceInfra.updateInstance(instance);
            return null;
        }

        // 真正探测：尝试 listTools() 检查连接是否存活
        try {
            client.listTools();
            // 探测成功，更新心跳时间
            instance.setLastHeartbeat(LocalDateTime.now());
            instance.setFailCount(0);
            mcpInstanceInfra.updateHeartbeat(instance.getId());
        } catch (Exception e) {
            // 探测失败，连接已死
            log.warn("MCP 状态探测失败，连接已断开: configId={}, error={}", mcpConfigId, e.getMessage());
            mcpClientCache.remove(instance.getInstanceKey());
            try {
                client.closeGracefully();
            } catch (Exception ignored) {}

            instance.setStatus("ERROR");
            instance.setErrorMsg("连接探测失败: " + e.getMessage());
            mcpInstanceInfra.updateInstance(instance);
        }

        return instance;
    }

    /**
     * 获取 MCP 客户端
     */
    public McpSyncClient getMcpClient(Long mcpConfigId) {
        McpInstanceEntity instance = mcpInstanceInfra.getRunningByConfigId(mcpConfigId);
        if (instance == null) {
            return null;
        }
        return mcpClientCache.get(instance.getInstanceKey());
    }

    /**
     * 获取运行中的实例
     */
    public McpInstanceEntity getRunningInstance(Long mcpConfigId) {
        return mcpInstanceInfra.getRunningByConfigId(mcpConfigId);
    }

    /**
     * 获取所有真正运行中的实例
     *
     * 过滤掉 client 已丢失的"孤儿"记录
     */
    public List<McpInstanceEntity> getAllRunningInstances() {
        List<McpInstanceEntity> running = mcpInstanceInfra.listRunning();

        // 清理 client 已丢失的孤儿记录
        List<McpInstanceEntity> alive = new ArrayList<>();
        for (McpInstanceEntity instance : running) {
            if (mcpClientCache.containsKey(instance.getInstanceKey())) {
                alive.add(instance);
            } else {
                // 孤儿记录：数据库标记 RUNNING 但内存无 client
                log.warn("清理孤儿实例记录: configId={}, instanceKey={}",
                        instance.getMcpConfigId(), instance.getInstanceKey());
                instance.setStatus("STOPPED");
                instance.setStopTime(LocalDateTime.now());
                instance.setErrorMsg("服务重启导致连接丢失");
                mcpInstanceInfra.updateInstance(instance);
            }
        }

        return alive;
    }

    /**
     * 根据 instanceKey 获取实例
     */
    public McpInstanceEntity getByInstanceKey(String instanceKey) {
        return mcpInstanceInfra.getByInstanceKey(instanceKey);
    }

    // ==================== 客户端创建 ====================

    /**
     * 创建 MCP 客户端
     */
    private McpSyncClient createMcpClient(McpConfigEntity config, String instanceKey) throws Exception {
        String transportType = config.getTransportType();

        if ("STDIO".equalsIgnoreCase(transportType) || "UPLOADED".equalsIgnoreCase(transportType)) {
            // STDIO 模式（UPLOADED 也使用 STDIO，只是多了下载解压步骤）
            return createStdioClient(config);
        } else {
            // REMOTE / SSE 模式
            return createHttpClient(config);
        }
    }

    /**
     * 创建 STDIO 客户端
     *
     * UPLOADED 模式流程：从 S3 下载 ZIP → 解压到本地 → shell 包装器设置工作目录 → 启动进程
     * STDIO 模式流程：直接使用配置的命令启动进程
     */
    private McpSyncClient createStdioClient(McpConfigEntity config) throws Exception {
        String effectiveCommand = config.getCommand();
        List<String> effectiveArgs = parseArgs(config.getArgs());
        Map<String, String> effectiveEnv = parseEnv(config.getEnv());

        // UPLOADED 模式：下载并解压 ZIP 包
        if ("UPLOADED".equalsIgnoreCase(config.getTransportType())) {
            String extractedPath = prepareWorkDir(config);
            File workDir = new File(extractedPath);

            // 如果用户没有指定 command，自动检测入口文件
            if (effectiveCommand == null || effectiveCommand.isBlank()) {
                String entryPoint = detectEntryPoint(workDir);

                // 如果未检测到入口文件，尝试 npm run build 构建
                if (entryPoint == null) {
                    log.info("入口文件未找到，尝试构建...");
                    runBuildIfNeeded(workDir);
                    entryPoint = detectEntryPoint(workDir);
                }

                // 默认使用 node 启动检测到的入口文件
                effectiveCommand = "node";
                if (entryPoint != null) {
                    effectiveArgs = new ArrayList<>(effectiveArgs);
                    effectiveArgs.add(0, entryPoint);
                }
                log.info("UPLOADED 自动检测: command={}, entryPoint={}", effectiveCommand, entryPoint);
            }

            // 构建完整命令: <command> <args>
            StringBuilder cmdBuilder = new StringBuilder(effectiveCommand);
            for (String arg : effectiveArgs) {
                cmdBuilder.append(" ").append(arg);
            }
            String originalCommand = cmdBuilder.toString();

            // 使用 shell 包装器切换工作目录
            String osName = System.getProperty("os.name", "").toLowerCase();
            if (osName.contains("win")) {
                effectiveCommand = "cmd";
                effectiveArgs = List.of("/c", "cd /d \"" + extractedPath + "\" && " + originalCommand);
            } else {
                effectiveCommand = "sh";
                effectiveArgs = List.of("-c", "cd \"" + extractedPath + "\" && " + originalCommand);
            }

            log.info("UPLOADED 模式启动: cwd={}, command={}", extractedPath, originalCommand);
        } else {
            // 纯 STDIO 模式，支持 workDir 配置
            if (StringUtils.hasText(config.getWorkDir())) {
                String originalCommand = effectiveCommand + " " + String.join(" ", effectiveArgs);

                String osName = System.getProperty("os.name", "").toLowerCase();
                if (osName.contains("win")) {
                    effectiveCommand = "cmd";
                    effectiveArgs = List.of("/c", "cd /d \"" + config.getWorkDir() + "\" && " + originalCommand);
                } else {
                    effectiveCommand = "sh";
                    effectiveArgs = List.of("-c", "cd \"" + config.getWorkDir() + "\" && " + originalCommand);
                }
            }
        }

        // 构建 ServerParameters
        ServerParameters.Builder builder = ServerParameters.builder(effectiveCommand);
        if (!effectiveArgs.isEmpty()) {
            builder.args(effectiveArgs.toArray(new String[0]));
        }
        if (!effectiveEnv.isEmpty()) {
            builder.env(effectiveEnv);
        }

        ServerParameters params = builder.build();

        // 创建 Jackson McpJsonMapper
        JsonMapper jsonMapper = JsonMapper.builder().build();
        JacksonMcpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(jsonMapper);

        // 创建 STDIO 传输
        StdioClientTransport transport = new StdioClientTransport(params, mcpJsonMapper);

        // 创建 MCP 客户端
        return McpClient.sync(transport)
            .requestTimeout(Duration.ofSeconds(30))
            .build();
    }

    /**
     * 创建 HTTP 客户端
     *
     * 根据 protocol 字段选择传输协议：
     * - "SSE" 或空（兼容旧数据）→ HttpClientSseClientTransport（旧版 SSE，连接 /sse）
     * - "STREAMABLE_HTTP" → HttpClientStreamableHttpTransport（新版 Streamable HTTP，连接 /mcp）
     */
    private McpSyncClient createHttpClient(McpConfigEntity config) {
        if (!StringUtils.hasText(config.getServerUrl())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "REMOTE 模式必须配置服务器地址");
        }

        String protocol = config.getProtocol();

        if ("STREAMABLE_HTTP".equalsIgnoreCase(protocol)) {
            log.info("使用 Streamable HTTP Transport 连接: {}", config.getServerUrl());
            HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(
                    config.getServerUrl()
            ).connectTimeout(Duration.ofSeconds(10)).build();
            return McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(30))
                    .build();
        } else {
            log.info("使用 SSE Transport 连接: {}", config.getServerUrl());
            HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(
                    config.getServerUrl()
            ).connectTimeout(Duration.ofSeconds(10)).build();
            return McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(30))
                    .build();
        }
    }

    // ==================== ZIP 下载与解压 ====================

    /**
     * 准备工作目录：从 S3 下载 ZIP 并解压
     *
     * 目录结构：{baseDir}/{configId}/
     * 如果 ZIP 包含单一根目录（如 GitHub 下载的 servers-main/），自动提升到该目录
     *
     * @return 解压后的有效工作目录绝对路径
     */
    private String prepareWorkDir(McpConfigEntity config) throws Exception {
        if (!StringUtils.hasText(config.getPackagePath())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "UPLOADED 模式未配置包路径");
        }

        // 目标目录: baseDir/configId/
        File workDir = new File(baseDir, String.valueOf(config.getId()));

        // 如果目录已存在且有内容，检查依赖后复用
        if (workDir.exists() && workDir.isDirectory()) {
            File[] children = workDir.listFiles();
            if (children != null && children.length > 0) {
                File effectiveDir = detectEffectiveDir(workDir);
                // 复用前检查依赖是否完整
                ensureDependencies(effectiveDir);
                log.info("工作目录已存在，直接复用: {}", effectiveDir.getAbsolutePath());
                return effectiveDir.getAbsolutePath();
            }
        }

        // 从 S3 下载 ZIP 包
        log.info("从 S3 下载 MCP ZIP 包: {}", config.getPackagePath());
        byte[] zipData = fileStorageUtil.download(config.getPackagePath());
        log.info("ZIP 包下载完成，大小: {} bytes", zipData.length);

        // 清空目录（如果存在残留）
        if (workDir.exists()) {
            deleteDirectory(workDir);
        }
        workDir.mkdirs();

        // 解压 ZIP
        extractZip(zipData, workDir);

        // 检测单根目录结构
        File effectiveDir = detectEffectiveDir(workDir);
        log.info("ZIP 解压完成，工作目录: {}", effectiveDir.getAbsolutePath());

        // 全新解压：清理不完整的 node_modules 并重新安装依赖
        installDependencies(effectiveDir);

        return effectiveDir.getAbsolutePath();
    }

    /**
     * 解压 ZIP 数据到目标目录
     */
    private void extractZip(byte[] zipData, File targetDir) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outputFile = new File(targetDir, entry.getName());

                // 安全检查：防止 ZIP 滑动攻击（Zip Slip）
                if (!outputFile.getCanonicalPath().startsWith(targetDir.getCanonicalPath() + File.separator)
                        && !outputFile.getCanonicalPath().equals(targetDir.getCanonicalPath())) {
                    throw new SecurityException("ZIP 条目超出目标目录: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    // 确保父目录存在
                    outputFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }

                    // 保留 Unix 可执行权限
                    if (entry.getName().endsWith(".sh") || entry.getName().equals("node_modules/.bin/*")) {
                        outputFile.setExecutable(true);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * 检测有效工作目录
     *
     * 如果 ZIP 解压后只有一个顶层目录（GitHub 下载格式如 servers-main/），
     * 自动提升到该目录作为工作目录
     */
    private File detectEffectiveDir(File workDir) {
        File[] topItems = workDir.listFiles(File::isDirectory);
        File[] topFiles = workDir.listFiles(File::isFile);

        // 只有一个子目录且没有顶层文件 → GitHub ZIP 格式
        if (topItems != null && topItems.length == 1 && (topFiles == null || topFiles.length == 0)) {
            File singleDir = topItems[0];
            log.info("检测到单根目录结构: {}，自动提升为工作目录", singleDir.getName());
            return singleDir;
        }

        return workDir;
    }

    /**
     * 自动检测 Node.js 包入口文件
     *
     * 优先级：package.json 的 bin 字段 > main 字段 > 常见路径猜测
     * 返回相对路径（相对于工作目录）
     */
    private String detectEntryPoint(File workDir) {
        File packageJsonFile = new File(workDir, "package.json");
        if (!packageJsonFile.exists()) {
            log.warn("未找到 package.json，跳过入口检测");
            return null;
        }

        try {
            String content = Files.readString(packageJsonFile.toPath());
            Map<String, Object> pkg = JSONUtil.toBean(content, Map.class);

            // 1. 优先查找 bin 字段（MCP server 标准入口）
            Object binObj = pkg.get("bin");
            if (binObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> bin = (Map<String, String>) binObj;
                if (!bin.isEmpty()) {
                    String entry = bin.values().iterator().next();
                    // 去掉开头的 ./
                    entry = entry.replaceFirst("^\\./", "");
                    if (new File(workDir, entry).exists()) {
                        log.info("从 bin 字段检测到入口: {}", entry);
                        return entry;
                    }
                }
            } else if (binObj instanceof String) {
                String entry = ((String) binObj).replaceFirst("^\\./", "");
                if (new File(workDir, entry).exists()) {
                    log.info("从 bin 字段检测到入口: {}", entry);
                    return entry;
                }
            }

            // 2. 查找 main 字段
            Object mainObj = pkg.get("main");
            if (mainObj instanceof String) {
                String entry = ((String) mainObj).replaceFirst("^\\./", "");
                if (new File(workDir, entry).exists()) {
                    log.info("从 main 字段检测到入口: {}", entry);
                    return entry;
                }
            }

            // 3. 常见路径猜测
            String[] commonPaths = {"dist/index.js", "build/index.js", "index.js", "src/index.js"};
            for (String path : commonPaths) {
                if (new File(workDir, path).exists()) {
                    log.info("从常见路径检测到入口: {}", path);
                    return path;
                }
            }

            log.warn("未能自动检测入口文件，将使用用户配置的命令");
            return null;

        } catch (Exception e) {
            log.warn("解析 package.json 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 尝试执行 npm run build（如果 package.json 中有 build 脚本）
     */
    private void runBuildIfNeeded(File workDir) throws Exception {
        File packageJsonFile = new File(workDir, "package.json");
        if (!packageJsonFile.exists()) {
            return;
        }

        try {
            String content = Files.readString(packageJsonFile.toPath());
            @SuppressWarnings("unchecked")
            Map<String, Object> pkg = JSONUtil.toBean(content, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> scripts = (Map<String, Object>) pkg.get("scripts");
            if (scripts == null || !scripts.containsKey("build")) {
                log.info("package.json 没有 build 脚本，跳过构建");
                return;
            }

            log.info("执行 npm run build...");
            long startTime = System.currentTimeMillis();

            String npmCmd = System.getProperty("os.name", "").toLowerCase().contains("win") ? "npm.cmd" : "npm";
            ProcessBuilder pb = new ProcessBuilder(npmCmd, "run", "build");
            pb.directory(workDir);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            long elapsed = System.currentTimeMillis() - startTime;

            if (exitCode == 0) {
                log.info("npm run build 完成，耗时: {}ms", elapsed);
            } else {
                log.warn("npm run build 失败，exitCode: {}，输出:\n{}", exitCode, output);
            }
        } catch (Exception e) {
            log.warn("构建过程异常: {}", e.getMessage());
        }
    }

    /**
     * 自动安装依赖（仅全新解压时调用）
     *
     * ZIP 中的 node_modules 可能不完整（monorepo 依赖提升等原因），
     * 先删除再重新安装，确保依赖完整
     */
    private void installDependencies(File workDir) throws Exception {
        File packageJson = new File(workDir, "package.json");
        if (!packageJson.exists()) {
            return;
        }

        // 删除 ZIP 中可能不完整的 node_modules
        File nodeModules = new File(workDir, "node_modules");
        if (nodeModules.exists()) {
            log.info("清理 ZIP 中不完整的 node_modules...");
            deleteDirectory(nodeModules);
        }

        runNpmInstall(workDir);
    }

    /**
     * 确保依赖已安装（复用目录时调用）
     *
     * 检查 node_modules/.package-lock.json 是否存在，
     * 如果不存在说明依赖安装不完整或从未执行过
     */
    private void ensureDependencies(File workDir) throws Exception {
        File packageJson = new File(workDir, "package.json");
        if (!packageJson.exists()) {
            return;
        }

        File nodeModules = new File(workDir, "node_modules");
        File packageLock = new File(nodeModules, ".package-lock.json");

        if (packageLock.exists()) {
            log.info("依赖已安装，跳过 npm install");
            return;
        }

        // 依赖不完整，重新安装
        log.info("检测到依赖不完整，重新安装...");
        if (nodeModules.exists()) {
            deleteDirectory(nodeModules);
        }
        runNpmInstall(workDir);
    }

    /**
     * 执行 npm install
     */
    private void runNpmInstall(File workDir) throws Exception {
        log.info("开始安装依赖 (npm install)...");
        long startTime = System.currentTimeMillis();

        // Windows 下 npm 是 .cmd 脚本，需要用 npm.cmd
        String npmCmd = System.getProperty("os.name", "").toLowerCase().contains("win") ? "npm.cmd" : "npm";
        // --ignore-scripts: 跳过 prepare/build 脚本，只安装依赖包
        ProcessBuilder pb = new ProcessBuilder(npmCmd, "install", "--ignore-scripts");
        pb.directory(workDir);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 读取输出（防止缓冲区满导致阻塞）
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        long elapsed = System.currentTimeMillis() - startTime;

        if (exitCode == 0) {
            log.info("npm install 完成，耗时: {}ms", elapsed);
        } else {
            log.error("npm install 失败，exitCode: {}，输出:\n{}", exitCode, output);
            throw new RuntimeException("npm install 失败 (exitCode=" + exitCode + ")");
        }
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        dir.delete();
    }

    // ==================== 参数解析 ====================

    /**
     * 解析命令参数
     */
    private List<String> parseArgs(String args) {
        if (!StringUtils.hasText(args)) {
            return List.of();
        }

        // 尝试解析为 JSON 数组
        try {
            if (args.trim().startsWith("[")) {
                return JSONUtil.toList(args, String.class);
            }
        } catch (Exception e) {
            log.warn("解析 JSON 数组失败，尝试按空格分割: {}", args, e);
        }

        // 兼容旧格式：按空格分割
        return List.of(args.split("\\s+"));
    }

    /**
     * 解析环境变量
     */
    private Map<String, String> parseEnv(String env) {
        if (!StringUtils.hasText(env)) {
            return Map.of();
        }
        try {
            return JSONUtil.toBean(env, Map.class);
        } catch (Exception e) {
            log.warn("解析环境变量失败: {}", env);
            return Map.of();
        }
    }
}
