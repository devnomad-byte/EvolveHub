package org.evolve.admin.api;

import org.evolve.admin.response.S3BucketResponse;
import org.evolve.admin.response.S3FileResponse;
import org.evolve.admin.service.*;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * S3 文件浏览器管理
 */
@RestController
@RequestMapping("/s3")
public class S3BrowserController {

    @Resource
    private S3BucketListManager bucketListManager;

    @Resource
    private S3FileListManager fileListManager;

    @Resource
    private S3FileUploadManager fileUploadManager;

    @Resource
    private S3FileDeleteManager fileDeleteManager;

    @Resource
    private S3FolderCreateManager folderCreateManager;

    @Resource
    private S3FileRenameManager fileRenameManager;

    @Resource
    private S3FileDownloadManager fileDownloadManager;

    /**
     * 获取 Bucket 列表
     */
    @GetMapping("/buckets")
    public Result<List<S3BucketResponse>> listBuckets() {
        return Result.ok(bucketListManager.execute());
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/files")
    public Result<List<S3FileResponse>> listFiles(
            @RequestParam String bucketName,
            @RequestParam(required = false) String path,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "100") int pageSize) {
        return Result.ok(fileListManager.execute(
                new S3FileListManager.Request(bucketName, path, pageNum, pageSize)));
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<Void> uploadFile(
            @RequestParam String bucketName,
            @RequestParam(required = false) String path,
            @RequestParam("file") MultipartFile file) {
        fileUploadManager.execute(new S3FileUploadManager.Request(bucketName, path, file));
        return Result.ok();
    }

    /**
     * 删除文件或文件夹
     */
    @DeleteMapping("/files")
    public Result<Void> deleteFile(
            @RequestParam String bucketName,
            @RequestParam String objectKey) {
        fileDeleteManager.execute(new S3FileDeleteManager.Request(bucketName, objectKey));
        return Result.ok();
    }

    /**
     * 创建文件夹
     */
    @PostMapping("/folders")
    public Result<Void> createFolder(
            @RequestParam String bucketName,
            @RequestParam(required = false) String folderPath,
            @RequestParam String folderName) {
        folderCreateManager.execute(new S3FolderCreateManager.Request(bucketName, folderPath, folderName));
        return Result.ok();
    }

    /**
     * 重命名文件或文件夹
     */
    @PutMapping("/rename")
    public Result<Void> rename(
            @RequestParam String bucketName,
            @RequestParam String sourceKey,
            @RequestParam String targetName) {
        fileRenameManager.execute(new S3FileRenameManager.Request(bucketName, sourceKey, targetName));
        return Result.ok();
    }

    /**
     * 下载文件
     */
    @GetMapping("/download")
    public org.springframework.http.ResponseEntity<byte[]> downloadFile(
            @RequestParam String bucketName,
            @RequestParam String objectKey) {
        byte[] content = fileDownloadManager.execute(
                new S3FileDownloadManager.Request(bucketName, objectKey));

        // 提取文件名
        String fileName = objectKey;
        int lastSlash = objectKey.lastIndexOf('/');
        if (lastSlash >= 0) {
            fileName = objectKey.substring(lastSlash + 1);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return new org.springframework.http.ResponseEntity<>(content, headers, org.springframework.http.HttpStatus.OK);
    }
}
