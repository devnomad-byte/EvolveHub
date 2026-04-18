package org.evolve.admin.service;

import org.evolve.admin.utils.FileStorageUtil;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

/**
 * S3 文件上传管理
 */
@Service
public class S3FileUploadManager extends BaseManager<S3FileUploadManager.Request, Void> {

    public record Request(
            String bucketName,
            String path,
            MultipartFile file
    ) {}

    @Resource
    private FileStorageUtil fileStorageUtil;

    @Override
    protected void check(Request request) {
        if (request.bucketName() == null || request.bucketName().isBlank()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "bucketName 不能为空");
        }
        if (request.file() == null || request.file().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "文件不能为空");
        }
    }

    @Override
    protected Void process(Request request) {
        try {
            String objectKey = buildObjectKey(request.path(), request.file().getOriginalFilename());
            fileStorageUtil.upload(
                    objectKey,
                    request.file().getInputStream(),
                    request.file().getSize(),
                    request.file().getContentType()
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "上传文件失败: " + e.getMessage());
        }
        return null;
    }

    private String buildObjectKey(String path, String filename) {
        if (filename == null || filename.isBlank()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "文件名不能为空");
        }
        if (path == null || path.isBlank()) {
            return filename;
        }
        if (path.endsWith("/")) {
            return path + filename;
        }
        return path + "/" + filename;
    }
}
