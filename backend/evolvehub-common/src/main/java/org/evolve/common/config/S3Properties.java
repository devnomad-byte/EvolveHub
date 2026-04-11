package org.evolve.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO / S3 存储配置。
 */
@Data
@ConfigurationProperties(prefix = "s3")
public class S3Properties {

    /**
     * 是否启用 S3 存储能力。
     */
    private boolean enabled = false;

    /**
     * 区域，MinIO 场景通常可保持默认值。
     */
    private String region = "us-east-1";

    /**
     * MinIO/S3 访问端点，例如 http://127.0.0.1:9000。
     */
    private String endpoint;

    /**
     * AccessKey。
     */
    private String accessKey;

    /**
     * SecretKey。
     */
    private String secretKey;

    /**
     * 默认桶名。
     */
    private String bucket;

    /**
     * MinIO 建议开启 path-style。
     */
    private boolean pathStyleAccessEnabled = true;

    /**
     * 启动时自动创建 bucket。
     */
    private boolean autoCreateBucket = true;
}
