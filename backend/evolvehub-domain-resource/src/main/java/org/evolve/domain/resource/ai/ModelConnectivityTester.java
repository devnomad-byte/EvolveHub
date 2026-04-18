package org.evolve.domain.resource.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
            String friendlyMessage = getFriendlyErrorMessage(e);
            return TestResult.fail(friendlyMessage);
        }
    }

    /**
     * OpenAI 兼容接口测试：GET /v1/models
     * <p>
     * 此端点仅返回可用模型列表，不消耗 token，是最轻量的验证方式
     */
    private TestResult testOpenAICompatible(String baseUrl, String apiKey) {
        // 兼容 baseUrl 已携带 /v1 的情况，自动去除末尾的 /v1 再拼接
        String normalized = stripTrailingSlash(baseUrl);
        if (normalized.endsWith("/v1")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        }
        String url = normalized + "/v1/models";
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
        return TestResult.fail(httpStatusMessage(response.getStatusCode().value()));
    }

    /**
     * Anthropic 接口测试：POST /v1/messages
     * <p>
     * Anthropic 使用 x-api-key 头，且没有 /models 端点，
     * 发送一个 max_tokens=1 的请求来验证密钥有效性
     */
    private TestResult testAnthropic(String baseUrl, String apiKey) {
        // 兼容 baseUrl 已携带 /v1 的情况
        String normalized = stripTrailingSlash(baseUrl);
        if (normalized.endsWith("/v1")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        }
        String url = normalized + "/v1/messages";
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
        return TestResult.fail(httpStatusMessage(response.getStatusCode().value()));
    }

    /**
     * 将 HTTP 状态码转换为用户友好的错误消息
     */
    private String httpStatusMessage(int statusCode) {
        return switch (statusCode) {
            case 401 -> "API密钥无效（401 Unauthorized）";
            case 403 -> "API密钥权限不足（403 Forbidden），请检查是否具有访问权限";
            case 404 -> "API地址错误（404 Not Found），请检查baseUrl是否正确";
            case 429 -> "请求频率超限（429 Too Many Requests），请稍后重试";
            default -> {
                if (statusCode >= 500) {
                    yield "API服务器错误（" + statusCode + "），可能是服务商问题";
                } else {
                    yield "API返回状态码: " + statusCode;
                }
            }
        };
    }

    /**
     * 将异常转换为用户友好的错误消息
     */
    private String getFriendlyErrorMessage(Exception e) {
        if (e instanceof SocketTimeoutException) {
            return "连接超时，请检查网络或API地址是否正确";
        }
        if (e instanceof UnknownHostException) {
            return "无法解析服务器地址，请检查baseUrl是否正确";
        }
        if (e instanceof ResourceAccessException) {
            Throwable cause = e.getCause();
            if (cause instanceof SocketTimeoutException) {
                return "连接超时，请检查网络或API地址是否正确";
            }
            if (cause instanceof UnknownHostException) {
                return "无法解析服务器地址，请检查baseUrl是否正确";
            }
            String msg = cause != null ? cause.getMessage() : e.getMessage();
            if (msg != null && msg.contains("Connection refused")) {
                return "连接被拒绝，请检查baseUrl和端口是否正确";
            }
            if (msg != null && msg.contains("Connect timed out")) {
                return "连接超时，请检查网络或API地址是否正确";
            }
            return "网络连接失败，请检查网络和API地址";
        }
        if (e instanceof HttpClientErrorException) {
            return httpStatusMessage(((HttpClientErrorException) e).getStatusCode().value());
        }
        if (e instanceof HttpServerErrorException) {
            return httpStatusMessage(((HttpServerErrorException) e).getStatusCode().value());
        }
        // 去掉 Spring 的 I/O error 前缀，只留核心错误信息
        String msg = e.getMessage();
        if (msg != null) {
            int idx = msg.indexOf(": ");
            if (idx > 0) {
                return msg.substring(idx + 2).trim();
            }
        }
        return "连接失败: " + e.getClass().getSimpleName();
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
