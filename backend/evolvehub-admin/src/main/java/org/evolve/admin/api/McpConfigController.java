package org.evolve.admin.api;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.admin.request.CreateMcpConfigRequest;
import org.evolve.admin.request.UpdateMcpConfigRequest;
import org.evolve.admin.response.CreateMcpConfigResponse;
import org.evolve.admin.response.McpToolResponse;
import org.evolve.admin.response.UpdateMcpConfigResponse;
import org.evolve.admin.service.*;
import org.evolve.common.utils.S3Util;
import org.evolve.domain.resource.infra.McpToolInfra;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * MCP 配置管理控制器
 *
 * @author zhao
 */
@RestController
@RequestMapping("/mcp-config")
public class McpConfigController {

    @Resource
    private CreateMcpConfigManager createMcpConfigManager;

    @Resource
    private UpdateMcpConfigManager updateMcpConfigManager;

    @Resource
    private GetMcpConfigManager getMcpConfigManager;

    @Resource
    private ListMcpConfigManager listMcpConfigManager;

    @Resource
    private DeleteMcpConfigManager deleteMcpConfigManager;

    @Resource
    private GetMcpToolManager getMcpToolManager;

    @Resource
    private ListMcpToolManager listMcpToolManager;

    @Resource
    private McpToolInfra mcpToolInfra;

    /**
     * 创建 MCP 配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/create")
    public Result<CreateMcpConfigResponse> create(@RequestBody @Valid CreateMcpConfigRequest request) {
        return Result.ok(createMcpConfigManager.execute(request));
    }

    /**
     * 根据 ID 查询 MCP 配置详情
     */
    @GetMapping("/{id}")
    public Result<McpConfigEntity> getById(@PathVariable Long id) {
        return Result.ok(getMcpConfigManager.execute(id));
    }

    /**
     * 分页查询 MCP 配置列表（所有 scope）
     */
    @GetMapping("/list")
    public Result<PageResponse<McpConfigEntity>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(listMcpConfigManager.execute(new PageRequest(pageNum, pageSize)));
    }

    /**
     * 更新 MCP 配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @PutMapping("/update")
    public Result<UpdateMcpConfigResponse> update(@RequestBody @Valid UpdateMcpConfigRequest request) {
        return Result.ok(updateMcpConfigManager.execute(request));
    }

    /**
     * 删除 MCP 配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deleteMcpConfigManager.execute(id);
        return Result.ok();
    }

    /**
     * 启用/禁用 MCP 配置
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/{id}/toggle")
    public Result<UpdateMcpConfigResponse> toggle(@PathVariable Long id) {
        McpConfigEntity existing = getMcpConfigManager.execute(id);
        UpdateMcpConfigRequest request = new UpdateMcpConfigRequest(
                id, null, null, null, null, null, null, null, null,
                existing.getProtocol(), null, existing.getEnabled() == 1 ? 0 : 1,
                existing.getScope(), existing.getDeptId()
        );
        return Result.ok(updateMcpConfigManager.execute(request));
    }

    /**
     * 获取 MCP 下的工具列表
     */
    @GetMapping("/{id}/tools")
    public Result<List<McpToolResponse>> listTools(@PathVariable Long id) {
        // 使用工具 Infra 直接查询
        var tools = mcpToolInfra.listByMcpConfigId(id);
        List<McpToolResponse> response = tools.stream()
                .map(tool -> {
                    McpToolResponse r = new McpToolResponse();
                    r.setId(tool.getId());
                    r.setMcpConfigId(tool.getMcpConfigId());
                    r.setName(tool.getName());
                    r.setDescription(tool.getDescription());
                    r.setInputSchema(tool.getInputSchema());
                    r.setRiskLevel(tool.getRiskLevel());
                    r.setToolScope(tool.getToolScope());
                    r.setDeptId(tool.getDeptId());
                    r.setOwnerId(tool.getOwnerId());
                    r.setEnabled(tool.getEnabled());
                    return r;
                })
                .toList();
        return Result.ok(response);
    }

    /**
     * 上传 MCP Server ZIP 包
     */
    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/upload")
    public Result<UploadResponse> uploadMcpZip(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "overwrite", required = false, defaultValue = "false") boolean overwrite,
            @RequestParam(value = "target_name", required = false) String targetName
    ) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.fail("文件不能为空");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.endsWith(".zip")) {
                return Result.fail("只支持 ZIP 格式的文件");
            }

            // 生成存储路径：mcp-servers/YYYY/MM/DD/uuid-filename.zip
            String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String fileName = (targetName != null ? targetName : originalFilename);
            String objectKey = String.format("mcp-servers/%s/%s-%s", datePrefix, uuid, fileName);

            // 上传到 MinIO/S3
            log.info("开始上传 MCP Server ZIP 包: {}, 大小: {} bytes", objectKey, file.getSize());
            s3Util.upload(bucket, objectKey, file.getInputStream(), file.getSize(), file.getContentType());

            log.info("MCP Server ZIP 包上传成功: {}", objectKey);

            // 返回结果
            UploadResponse response = new UploadResponse();
            response.setSuccess(true);
            response.setPackagePath(objectKey);
            response.setFileSize(file.getSize());
            response.setFileName(originalFilename);
            response.setMessage("文件上传成功");

            return Result.ok(response);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("上传 MCP Server ZIP 包时发生错误", e);
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    @Resource
    private S3Util s3Util;

    @Value("${s3.bucket}")
    private String bucket;

    private static final Logger log = LoggerFactory.getLogger(McpConfigController.class);

    /**
     * 上传响应
     */
    public static class UploadResponse {
        private boolean success;
        private String packagePath;
        private long fileSize;
        private String fileName;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getPackagePath() {
            return packagePath;
        }

        public void setPackagePath(String packagePath) {
            this.packagePath = packagePath;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
