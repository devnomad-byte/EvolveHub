package org.evolve.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.evolve.common.config.S3Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;

/**
 * MinIO / S3 存储工具类
 *
 * 提供文件上传、下载、删除等基本功能
 *
 * @author devnomad-byte
 * @version v1.0
 * @date 2026/04/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "s3", name = "enabled", havingValue = "true")
public class S3Util {

    private final S3Client s3Client;
    private final S3Properties properties;

    /**
     * 上传文件到 S3/MinIO（使用默认 bucket）
     *
     * @param objectKey    对象键（文件路径）
     * @param inputStream   输入流
     * @param contentLength 内容长度
     * @param contentType   内容类型
     */
    public void upload(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        upload(properties.getBucket(), objectKey, inputStream, contentLength, contentType);
    }

    /**
     * 上传文件到指定 bucket
     *
     * @param bucket        bucket 名称
     * @param objectKey     对象键（文件路径）
     * @param inputStream    输入流
     * @param contentLength  内容长度
     * @param contentType    内容类型
     */
    public void upload(String bucket, String objectKey, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));

            log.info("文件上传成功: bucket={}, key={}, size={}", bucket, objectKey, contentLength);

        } catch (S3Exception e) {
            log.error("S3 上传失败: bucket={}, key={}", bucket, objectKey, e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }
}
