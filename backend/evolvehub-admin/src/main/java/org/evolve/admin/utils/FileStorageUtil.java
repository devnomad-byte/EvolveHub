package org.evolve.admin.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.evolve.common.config.S3Properties;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件存储工具类 - 仅支持 MinIO S3
 */
@Slf4j
@Component
public class FileStorageUtil {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Properties s3Properties;

    /**
     * 获取当前配置的 bucket 名称
     */
    public String getBucket() {
        return s3Properties.getBucket();
    }

    /**
     * 列出所有 Bucket
     */
    public List<BucketInfo> listBuckets() {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            return response.buckets().stream()
                    .map(b -> new BucketInfo(b.name(), b.creationDate() != null ? b.creationDate().toString() : null))
                    .toList();
        } catch (Exception e) {
            log.error("[S3] 列出 buckets 失败", e);
            throw new BusinessException(ResultCode.FAIL, "列出 buckets 失败: " + e.getMessage());
        }
    }

    /**
     * 获取 Bucket 元数据（文件数、总大小）
     */
    public BucketMetadata getBucketMetadata(String bucketName) {
        try {
            long totalSize = 0;
            int fileCount = 0;
            String continuationToken = null;

            do {
                ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .maxKeys(1000);

                if (continuationToken != null) {
                    requestBuilder.continuationToken(continuationToken);
                }

                ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
                for (S3Object obj : response.contents()) {
                    totalSize += obj.size();
                    fileCount++;
                }
                continuationToken = response.nextContinuationToken();
            } while (continuationToken != null);

            return new BucketMetadata(bucketName, fileCount, totalSize);
        } catch (Exception e) {
            log.error("[S3] 获取 bucket 元数据失败: bucket={}", bucketName, e);
            throw new BusinessException(ResultCode.FAIL, "获取 bucket 元数据失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件
     */
    public void upload(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        uploadToS3(objectKey, inputStream, contentLength, contentType);
    }

    public void upload(String objectKey, byte[] data, String contentType) {
        uploadToS3(objectKey, data, contentType);
    }

    /**
     * 下载文件
     */
    public byte[] download(String objectKey) {
        return downloadFromS3(objectKey);
    }

    /**
     * 删除文件
     */
    public void delete(String objectKey) {
        deleteFromS3(objectKey);
    }

    /**
     * 检查文件是否存在
     */
    public boolean exists(String objectKey) {
        return existsInS3(objectKey);
    }

    /**
     * 列出指定前缀下的所有文件
     */
    public List<FileSummary> listFiles(String prefix) {
        return listFilesFromS3(s3Properties.getBucket(), prefix);
    }

    /**
     * 列出指定 bucket 和前缀下的所有文件
     */
    public List<FileSummary> listFiles(String bucketName, String prefix) {
        return listFilesFromS3(bucketName, prefix);
    }

    /**
     * 列出指定 bucket、前缀下的文件，支持 delimiter（文件夹浏览）
     */
    public ListObjectsResult listObjectsWithDelimiter(String bucketName, String prefix) {
        try {
            List<FileSummary> files = new ArrayList<>();
            List<String> folders = new ArrayList<>();
            String continuationToken = null;

            do {
                ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .delimiter("/")
                        .maxKeys(1000);

                if (continuationToken != null) {
                    requestBuilder.continuationToken(continuationToken);
                }

                ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());

                // 收集文件夹
                if (response.commonPrefixes() != null) {
                    for (var cp : response.commonPrefixes()) {
                        folders.add(cp.prefix());
                    }
                }

                // 收集文件
                if (response.contents() != null) {
                    for (S3Object s3Object : response.contents()) {
                        String key = s3Object.key();
                        // 跳过文件夹本身
                        if (prefix != null && key.equals(prefix)) {
                            continue;
                        }
                        FileSummary summary = new FileSummary();
                        summary.setKey(key);
                        summary.setSize(s3Object.size());
                        summary.setLastModified(s3Object.lastModified() != null ? s3Object.lastModified().toString() : null);
                        files.add(summary);
                    }
                }

                continuationToken = response.nextContinuationToken();
            } while (continuationToken != null);

            return new ListObjectsResult(files, folders);
        } catch (Exception e) {
            log.error("[S3] 列出文件失败: bucket={}, prefix={}", bucketName, prefix, e);
            throw new BusinessException(ResultCode.FAIL, "列出文件失败: " + e.getMessage());
        }
    }

    /**
     * 下载指定 bucket 中的文件
     */
    public byte[] download(String bucketName, String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            return s3Client.getObjectAsBytes(request).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ResultCode.NOT_FOUND, "S3 对象不存在: " + objectKey);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "从 S3 下载失败: " + objectKey);
        }
    }

    // ==================== S3 操作 ====================

    private void uploadToS3(String objectKey, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
            log.info("[S3] 上传成功: {}", objectKey);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "上传文件到 S3 失败: " + objectKey);
        }
    }

    private void uploadToS3(String objectKey, byte[] data, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(data));
            log.info("[S3] 上传成功: {}", objectKey);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "上传文件到 S3 失败: " + objectKey);
        }
    }

    private byte[] downloadFromS3(String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(objectKey)
                    .build();

            return s3Client.getObjectAsBytes(request).asByteArray();
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ResultCode.NOT_FOUND, "S3 对象不存在: " + objectKey);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "从 S3 下载失败: " + objectKey);
        }
    }

    private void deleteFromS3(String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(request);
            log.info("[S3] 删除成功: {}", objectKey);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAIL, "从 S3 删除失败: " + objectKey);
        }
    }

    private boolean existsInS3(String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(objectKey)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private List<FileSummary> listFilesFromS3(String bucketName, String prefix) {
        try {
            List<FileSummary> result = new ArrayList<>();
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            for (S3Object s3Object : response.contents()) {
                FileSummary summary = new FileSummary();
                summary.setKey(s3Object.key());
                summary.setSize(s3Object.size());
                summary.setLastModified(s3Object.lastModified() != null ? s3Object.lastModified().toString() : null);
                result.add(summary);
            }
            return result;
        } catch (Exception e) {
            log.error("[S3] 列出文件失败: prefix={}", prefix, e);
            return new ArrayList<>();
        }
    }

    /**
     * Bucket 信息
     */
    @Data
    public static class BucketInfo {
        private String name;
        private String creationDate;

        public BucketInfo(String name, String creationDate) {
            this.name = name;
            this.creationDate = creationDate;
        }
    }

    /**
     * Bucket 元数据
     */
    @Data
    public static class BucketMetadata {
        private String bucketName;
        private int fileCount;
        private long totalSize;

        public BucketMetadata(String bucketName, int fileCount, long totalSize) {
            this.bucketName = bucketName;
            this.fileCount = fileCount;
            this.totalSize = totalSize;
        }
    }

    /**
     * 文件摘要
     */
    @Data
    public static class FileSummary {
        private String key;
        private long size;
        private String lastModified;
    }

    /**
     * 列表对象结果（文件+文件夹）
     */
    @Data
    public static class ListObjectsResult {
        private List<FileSummary> files;
        private List<String> folders;

        public ListObjectsResult(List<FileSummary> files, List<String> folders) {
            this.files = files;
            this.folders = folders;
        }
    }
}
