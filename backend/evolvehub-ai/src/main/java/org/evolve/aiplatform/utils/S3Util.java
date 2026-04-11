package org.evolve.aiplatform.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.evolve.common.config.S3Properties;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

/**
 * MinIO 场景下的 S3 工具类。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "s3", name = "enabled", havingValue = "true")
public class S3Util {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    public void upload(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "上传文件到 MinIO 失败: " + objectKey);
        }
    }

    public void upload(String objectKey, byte[] data, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(data));
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "上传字节流到 MinIO 失败: " + objectKey);
        }
    }

    public byte[] download(String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
            return response.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ResultCode.NOT_FOUND, "对象不存在: " + objectKey);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "下载 MinIO 对象失败: " + objectKey);
        }
    }

    public void delete(String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "删除 MinIO 对象失败: " + objectKey);
        }
    }

    public boolean exists(String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw new BusinessException(ResultCode.FAIL, "检查 MinIO 对象是否存在失败: " + objectKey);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "检查 MinIO 对象是否存在失败: " + objectKey);
        }
    }

    public String presignGetUrl(String objectKey, Duration duration) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();

        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(objectRequest)
                .build();

        return s3Presigner.presignGetObject(request).url().toString();
    }

    public String presignPutUrl(String objectKey, Duration duration, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest request = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(objectRequest)
                .build();

        return s3Presigner.presignPutObject(request).url().toString();
    }

    public void ensureBucketExists() {
        String bucket = properties.getBucket();
        if (bucket == null || bucket.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "s3.bucket 未配置");
        }

        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                createBucket(bucket);
                return;
            }
            throw new BusinessException(ResultCode.FAIL, "检查 MinIO Bucket 失败: " + bucket);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "检查 MinIO Bucket 失败: " + bucket);
        }
    }

    private void createBucket(String bucket) {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("[MinIO] 已创建 bucket: {}", bucket);
        } catch (BucketAlreadyOwnedByYouException e) {
            log.info("[MinIO] bucket 已存在: {}", bucket);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "创建 MinIO Bucket 失败: " + bucket);
        }
    }
}
