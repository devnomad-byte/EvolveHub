package org.evolve.admin.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 前置连通性测试请求
 *
 * @author zhao
 */
@Getter
@Setter
public class TestConnectionRequest {

    /** 模型提供商（如 openai / deepseek / anthropic） */
    @NotBlank(message = "提供商不能为空")
    private String provider;

    /** 自定义基址URL（可选） */
    private String baseUrl;

    /** API密钥 */
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;
}
