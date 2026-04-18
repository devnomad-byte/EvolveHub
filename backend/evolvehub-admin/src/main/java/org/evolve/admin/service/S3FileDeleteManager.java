package org.evolve.admin.service;

import org.evolve.admin.utils.FileStorageUtil;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import jakarta.annotation.Resource;

/**
 * S3 文件删除管理
 */
@Service
public class S3FileDeleteManager extends BaseManager<S3FileDeleteManager.Request, Void> {

    public record Request(
            String bucketName,
            String objectKey
    ) {}

    @Resource
    private FileStorageUtil fileStorageUtil;

    @Resource
    private S3Client s3Client;

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
    protected Void process(Request request) {
        String objectKey = request.objectKey();

        // 检查是否是文件夹（以 / 结尾）
        if (objectKey.endsWith("/")) {
            // 删除文件夹内的所有对象
            deleteFolderContents(request.bucketName(), objectKey);
        } else {
            // 删除单个文件
            deleteObject(request.bucketName(), objectKey);
        }
        return null;
    }

    private void deleteObject(String bucketName, String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "删除文件失败: " + e.getMessage());
        }
    }

    private void deleteFolderContents(String bucketName, String folderPrefix) {
        try {
            String continuationToken = null;
            do {
                ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(folderPrefix)
                        .maxKeys(1000);

                if (continuationToken != null) {
                    requestBuilder.continuationToken(continuationToken);
                }

                ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());

                for (var obj : response.contents()) {
                    deleteObject(bucketName, obj.key());
                }

                continuationToken = response.nextContinuationToken();
            } while (continuationToken != null);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "删除文件夹失败: " + e.getMessage());
        }
    }
}
