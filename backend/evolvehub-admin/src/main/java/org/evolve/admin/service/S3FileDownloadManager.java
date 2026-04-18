package org.evolve.admin.service;

import org.evolve.admin.utils.FileStorageUtil;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * S3 文件下载管理
 */
@Service
public class S3FileDownloadManager extends BaseManager<S3FileDownloadManager.Request, byte[]> {

    public record Request(
            String bucketName,
            String objectKey
    ) {}

    @Resource
    private FileStorageUtil fileStorageUtil;

    @Override
    protected void check(Request request) {
        if (request.bucketName() == null || request.bucketName().isBlank()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "bucketName 不能为空");
        }
        if (request.objectKey() == null || request.objectKey().isBlank()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "objectKey 不能为空");
        }
    }

    @Override
    protected byte[] process(Request request) {
        try {
            return fileStorageUtil.download(request.bucketName(), request.objectKey());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "下载文件失败: " + e.getMessage());
        }
    }
}
