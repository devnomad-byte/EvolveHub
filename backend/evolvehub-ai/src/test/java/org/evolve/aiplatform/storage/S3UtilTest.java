package org.evolve.aiplatform.storage;

import org.evolve.aiplatform.AiPlatformApiApplication;
import org.evolve.aiplatform.utils.S3Util;
import org.evolve.common.config.S3Properties;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = AiPlatformApiApplication.class)
@ActiveProfiles("dev")
class S3UtilTest {

    @Autowired
    private S3Util s3Util;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Properties properties;

    @Test
    void uploadHelloTxtShouldSucceed() {
        Assumptions.assumeTrue(properties.isEnabled(), "s3.enabled=false, skip real S3 integration test.");
        Assumptions.assumeTrue(properties.getBucket() != null && !properties.getBucket().isBlank(), "s3.bucket is blank.");

        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        String objectKey = "test/hello-" + UUID.randomUUID() + ".txt";

        try {
            assertDoesNotThrow(s3Util::ensureBucketExists);
            assertDoesNotThrow(() -> s3Util.upload(objectKey, content, "text/plain"));

            byte[] downloaded = s3Util.download(objectKey);
            assertArrayEquals(content, downloaded);
        } finally {
            // 清理测试对象，避免污染 bucket。
             assertDoesNotThrow(() -> s3Util.delete(objectKey));
        }
    }
}
