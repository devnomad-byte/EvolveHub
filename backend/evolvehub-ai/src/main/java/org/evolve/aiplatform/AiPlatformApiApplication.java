package org.evolve.aiplatform;

import org.evolve.common.config.S3Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "org.evolve")
@Import(S3Config.class)
public class AiPlatformApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiPlatformApiApplication.class, args);
    }
}
