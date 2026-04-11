package org.evolve.aiplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * AI 平台应用启动类
 * <p>
 * 注意：
 * <ul>
 *     <li>Mapper 扫描由 {@link org.evolve.aiplatform.config.DataSourceConfig} 配置</li>
 *     <li>排除默认 DataSource 自动配置，使用自定义多数据源</li>
 * </ul>
 * </p>
 */
@SpringBootApplication(
        scanBasePackages = "org.evolve",
        exclude = {DataSourceAutoConfiguration.class}
)
public class AiPlatformApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiPlatformApiApplication.class, args);
    }
}
