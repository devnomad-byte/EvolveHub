package org.evolve.admin.service;

import org.evolve.admin.utils.FileStorageUtil;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;

/**
 * S3 文件夹创建管理
 */
@Service
public class S3FolderCreateManager extends BaseManager<S3FolderCreateManager.Request, Void> {

    public record Request(
            String bucketName,
            String folderPath,
            String folderName
    ) {}

    @Resource
    private FileStorageUtil fileStorageUtil;

    @Override
    protected void check(Request request) {
        if (request.bucketName() == null || request.bucketName().isBlank()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "bucketName 不能为空");
        }
        if (request.folderName() == null || request.folderName().isBlank()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "folderName 不能为空");
        }
        // 禁止特殊字符
        if (request.folderName().contains("/") || request.folderName().contains("\\")) {
            throw new BusinessException(ResultCode.BAD_PARAM, "文件夹名称不能包含 / 或 \\");
        }
    }

    @Override
    protected Void process(Request request) {
        try {
            String objectKey = buildFolderKey(request.folderPath(), request.folderName());
            // 上传空内容作为文件夹标记
            fileStorageUtil.upload(objectKey, new ByteArrayInputStream(new byte[0]), 0, "application/x-directory");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "创建文件夹失败: " + e.getMessage());
        }
        return null;
    }

    private String buildFolderKey(String path, String folderName) {
        String basePath = (path == null || path.isBlank()) ? "" : path;
        if (!basePath.isEmpty() && !basePath.endsWith("/")) {
            basePath += "/";
        }
        return basePath + folderName + "/";
    }
}
