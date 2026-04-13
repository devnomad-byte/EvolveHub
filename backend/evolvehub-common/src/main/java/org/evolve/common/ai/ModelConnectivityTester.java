package org.evolve.common.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * 模型连通性测试工具
 * <p>
 * 在保存模型配置前，通过向模型 API 发送轻量级请求来验证：
 * <ul>
 *     <li>baseUrl 是否可达</li>
 *     <li>apiKey 是否有效</li>
 * </ul>
 * <p>
 * 支持的提供商：
 * <ul>
 *     <li>OpenAI 兼容接口（OpenAI / DeepSeek / 通义千问 / 智谱 等）：GET /v1/models</li>
 *     <li>Anthropic：POST /v1/messages（使用 x-api-key 头）</li>
 * </ul>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Component
public class ModelConnectivityTester {

    private static final Logger log = LoggerFactory.getLogger(ModelConnectivityTester.class);

    /** 连接超时 5 秒 */
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    /** 读取超时 10 秒 */
    private static final int READ_TIMEOUT_MS = 10_000;

    /** 提供商默认 baseUrl 映射 */
    private static final Map<String, String> DEFAULT_BASE_URLS = Map.of(
            "openai", "https://api.openai.com",
            "deepseek", "https://api.deepseek.com",
            "anthropic", "https://api.anthropic.com"
    );

    private final RestClient restClient;

    public ModelConnectivityTester() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /**
     * 测试模型连通性
     *
     * @param provider 提供商标识（如 openai / deepseek / anthropic）
     * @param baseUrl  自定义基址，为空时使用提供商默认地址
     * @param apiKey   API 密钥
     * @return 测试结果
     */
    public TestResult test(String provider, String baseUrl, String apiKey) {
        String effectiveBaseUrl = resolveBaseUrl(provider, baseUrl);
        if (effectiveBaseUrl == null) {
            return TestResult.fail("无法确定 API 地址：未提供 baseUrl 且提供商 [" + provider + "] 无默认地址");
        }

        String normalizedProvider = provider.toLowerCase().trim();
        try {
            if ("anthropic".equals(normalizedProvider)) {
                return testAnthropic(effectiveBaseUrl, apiKey);
            }
            return testOpenAICompatible(effectiveBaseUrl, apiKey);
        } catch (Exception e) {
            log.warn("模型连通性测试异常: provider={}, baseUrl={}", provider, effectiveBaseUrl, e);
            return TestResult.fail("连接失败: " + e.getMessage());
        }
    }

    /**
     * OpenAI 兼容接口测试：GET /v1/models
     * <p>
     * 此端点仅返回可用模型列表，不消耗 token，是最轻量的验证方式
     */
    private TestResult testOpenAICompatible(String baseUrl, String apiKey) {
        String url = stripTrailingSlash(baseUrl) + "/v1/models";
        var response = restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    // 不抛异常，由下方逻辑处理
                })
                .toEntity(String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return TestResult.ok();
        }
        return TestResult.fail("API 返回状态码: " + response.getStatusCode().value());
    }

    /**
     * Anthropic 接口测试：POST /v1/messages
     * <p>
     * Anthropic 使用 x-api-key 头，且没有 /models 端点，
     * 发送一个 max_tokens=1 的请求来验证密钥有效性
     */
    private TestResult testAnthropic(String baseUrl, String apiKey) {
        String url = stripTrailingSlash(baseUrl) + "/v1/messages";
        String body = """
                {"model":"claude-3-haiku-20240307","max_tokens":1,"messages":[{"role":"user","content":"hi"}]}""";

        var response = restClient.post()
                .uri(url)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    // 不抛异常，由下方逻辑处理
                })
                .toEntity(String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return TestResult.ok();
        }
        return TestResult.fail("API 返回状态码: " + response.getStatusCode().value());
    }

    private String resolveBaseUrl(String provider, String baseUrl) {
        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl;
        }
        return DEFAULT_BASE_URLS.get(provider.toLowerCase().trim());
    }

    private String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * 连通性测试结果
     */
    public record TestResult(boolean success, String message) {

        public static TestResult ok() {
            return new TestResult(true, "连通性测试通过");
        }

        public static TestResult fail(String message) {
            return new TestResult(false, message);
        }
    }
}
