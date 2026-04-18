package org.evolve.admin.service;

import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import jakarta.annotation.Resource;

/**
 * S3 文件/文件夹重命名管理
 */
@Service
public class S3FileRenameManager extends BaseManager<S3FileRenameManager.Request, Void> {

    public record Request(
            String bucketName,
            String sourceKey,
            String targetName
    ) {}

    @Resource
    private S3Client s3Client;

    @Override
    protected void check(Request request) {
        if (request.bucketName() == null || request.bucketName().isBlank()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "bucketName 不能为空");
        }
        if (request.sourceKey() == null || request.sourceKey().isBlank()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "sourceKey 不能为空");
        }
        if (request.targetName() == null || request.targetName().isBlank()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "targetName 不能为空");
        }
        // 禁止特殊字符
        if (request.targetName().contains("/") || request.targetName().contains("\\")) {
            throw new BusinessException(ResultCode.BAD_PARAM, "名称不能包含 / 或 \\");
        }
    }

    @Override
    protected Void process(Request request) {
        String sourceKey = request.sourceKey();
        boolean isFolder = sourceKey.endsWith("/");

        if (isFolder) {
            renameFolder(request.bucketName(), sourceKey, request.targetName());
        } else {
            renameFile(request.bucketName(), sourceKey, request.targetName());
        }
        return null;
    }

    private void renameFile(String bucketName, String sourceKey, String targetName) {
        try {
            // 提取源路径
            int lastSlash = sourceKey.lastIndexOf('/');
            String targetKey;
            if (lastSlash > 0) {
                targetKey = sourceKey.substring(0, lastSlash + 1) + targetName;
            } else {
                targetKey = targetName;
            }

            // 复制到新名称
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(targetKey)
                    .build();
            s3Client.copyObject(copyRequest);

            // 删除原文件
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(sourceKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "重命名文件失败: " + e.getMessage());
        }
    }

    private void renameFolder(String bucketName, String sourcePrefix, String targetName) {
        try {
            // 获取父路径
            String parentPath = sourcePrefix;
            while (parentPath.endsWith("/")) {
                parentPath = parentPath.substring(0, parentPath.length() - 1);
            }
            int lastSlash = parentPath.lastIndexOf('/');
            String basePath = lastSlash > 0 ? parentPath.substring(0, lastSlash + 1) : "";

            // 获取所有对象
            String continuationToken = null;
            do {
                ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(sourcePrefix)
                        .maxKeys(1000);

                if (continuationToken != null) {
                    requestBuilder.continuationToken(continuationToken);
                }

                ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());

                for (var obj : response.contents()) {
                    String objKey = obj.key();
                    // 计算新 key
                    String newKey = basePath + targetName + "/" + objKey.substring(sourcePrefix.length());
                    // 复制
                    CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                            .sourceBucket(bucketName)
                            .sourceKey(objKey)
                            .destinationBucket(bucketName)
                            .destinationKey(newKey)
                            .build();
                    s3Client.copyObject(copyRequest);
                    // 删除
                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objKey)
                            .build();
                    s3Client.deleteObject(deleteRequest);
                }

                continuationToken = response.nextContinuationToken();
            } while (continuationToken != null);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "重命名文件夹失败: " + e.getMessage());
        }
    }
}
