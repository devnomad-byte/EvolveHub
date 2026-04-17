package org.evolve.admin.api;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.evolve.admin.service.SecurityScanner;
import org.evolve.admin.service.SkillFileService;
import org.evolve.admin.response.FileNode;
import org.evolve.admin.response.SecurityScanResult;
import org.evolve.common.web.response.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/skill-config")
public class SkillFileController {

    @Resource
    private SkillFileService skillFileService;

    @Resource
    private SecurityScanner securityScanner;

    /**
     * 列出技能工作区文件
     */
    @GetMapping("/{id}/files")
    public Result<List<FileNode>> listFiles(@PathVariable Long id) {
        return Result.ok(skillFileService.listFiles(id));
    }

    /**
     * 上传文件到技能工作区
     */
    @PostMapping("/{id}/file")
    public Result<?> uploadFile(@PathVariable Long id,
                                    @RequestParam("path") String path,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "skipScan", defaultValue = "false") boolean skipScan) {
        // 安全扫描（skipScan 仅允许跳过 MEDIUM/LOW）
        SecurityScanResult scanResult = securityScanner.scanFile(file);
        if (!scanResult.isPassed()) {
            if (!skipScan || !securityScanner.canBypass(scanResult)) {
                return Result.fail(4001, "安全扫描未通过", scanResult);
            }
        }

        skillFileService.uploadFile(id, path, file);
        return Result.ok();
    }

    /**
     * 下载文件
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id,
                                                @RequestParam("path") String path) {
        byte[] content = skillFileService.downloadFile(id, path);
        HttpHeaders headers = new HttpHeaders();
        // 根据文件扩展名设置 Content-Type
        String contentType = getContentType(path);
        headers.setContentType(MediaType.parseMediaType(contentType));
        return ResponseEntity.ok().headers(headers).body(content);
    }

    /**
     * 根据文件扩展名获取 Content-Type
     */
    private String getContentType(String path) {
        if (path == null) return "application/octet-stream";
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".md") || lowerPath.endsWith(".markdown")) {
            return "text/markdown; charset=utf-8";
        } else if (lowerPath.endsWith(".json")) {
            return "application/json; charset=utf-8";
        } else if (lowerPath.endsWith(".txt")) {
            return "text/plain; charset=utf-8";
        } else if (lowerPath.endsWith(".html") || lowerPath.endsWith(".htm")) {
            return "text/html; charset=utf-8";
        } else if (lowerPath.endsWith(".css")) {
            return "text/css; charset=utf-8";
        } else if (lowerPath.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        } else if (lowerPath.endsWith(".xml")) {
            return "application/xml; charset=utf-8";
        } else if (lowerPath.endsWith(".yaml") || lowerPath.endsWith(".yml")) {
            return "application/x-yaml; charset=utf-8";
        }
        return "application/octet-stream";
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}/file")
    public Result<Void> deleteFile(@PathVariable Long id, @RequestParam("path") String path) {
        skillFileService.deleteFile(id, path);
        return Result.ok();
    }

    /**
     * 创建文件夹
     */
    @PostMapping("/{id}/folder")
    public Result<Void> createFolder(@PathVariable Long id, @RequestParam("path") String path) {
        skillFileService.createFolder(id, path);
        return Result.ok();
    }
}
