package org.evolve.aiplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI 平台启动类
 *
 * @author TellyJiang
 * @since 2026-04-10
 */
@SpringBootApplication(scanBasePackages = "org.evolve")
public class AiPlatformApiApplication {

    /**
     * 启动 AI 平台聚合服务
     *
     * @param args 启动参数
     * @author TellyJiang
     * @since 2026-04-10
     */
    public static void main(String[] args) {
        SpringApplication.run(AiPlatformApiApplication.class, args);
    }
}
