package org.evolve.aiplatform.memory.framework.agentscope.algorithm.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.ChatResponse;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.config.ChatModelFactory;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionResultDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.aiplatform.memory.domain.constant.MemoryConstants;
import org.evolve.aiplatform.memory.framework.agentscope.algorithm.MemoryExtractionAlgorithm;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

/**
 * 默认记忆提取算法
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
@Component
public class DefaultMemoryExtractionAlgorithm implements MemoryExtractionAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(DefaultMemoryExtractionAlgorithm.class);

    private static final BigDecimal IMPORTANCE_THRESHOLD = new BigDecimal("0.500");

    private static final String EXTRACTION_PROMPT = """
            你是一个记忆提取助手。分析以下对话，提取用户透露的值得长期记住的信息。
            提取规则：
            1. 只提取用户稳定的偏好、习惯、背景事实、工具配置、长期任务约束
            2. 不要提取一次性的临时问题、无关寒暄和助手自己的表述
            3. 每条记忆使用一句简洁陈述句
            4. 为每条记忆给出 0.0 到 1.0 的 importance
            5. 如果没有值得记忆的内容，返回 []

            仅返回 JSON 数组，每项格式如下：
            [{"text":"用户喜欢咖啡","importance":0.8}]
            """;

    @Resource
    private ChatModelFactory chatModelFactory;

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public MemoryExtractionResultDTO extract(MemoryExtractionRequestDTO request) {
        if (request == null || request.getConversationContent() == null || request.getConversationContent().isBlank()) {
            return new MemoryExtractionResultDTO(List.of(), "memory extraction empty");
        }
        ModelConfigEntity modelConfig = findModelByNameIgnoreCase(request.getModelName());
        if (modelConfig == null) {
            log.warn("未找到记忆提取模型，跳过提取: modelName={}", request.getModelName());
            return new MemoryExtractionResultDTO(List.of(), "memory extraction empty");
        }
        try {
            ChatModelBase model = chatModelFactory.createModel(modelConfig);
            List<Msg> messages = List.of(
                    Msg.builder().role(MsgRole.SYSTEM).textContent(EXTRACTION_PROMPT).build(),
                    Msg.builder().role(MsgRole.USER).textContent(request.getConversationContent()).build()
            );
            String responseText = collectResponse(model.stream(messages, null, null));
            String normalizedJson = unwrapMarkdownCodeBlock(responseText);
            List<ExtractionItem> extracted = objectMapper.readValue(
                    normalizedJson,
                    new TypeReference<List<ExtractionItem>>() {}
            );
            List<MemoryStructuredItemDTO> items = extracted.stream()
                    .filter(item -> item != null && item.text() != null && !item.text().isBlank())
                    .filter(item -> normalizeImportance(item.importance()).compareTo(IMPORTANCE_THRESHOLD) >= 0)
                    .map(item -> new MemoryStructuredItemDTO(
                            request.getUserId(),
                            buildMemoryKey(request.getSessionId(), item.text()),
                            classifyMemoryType(item.text()),
                            item.text().trim(),
                            normalizeImportance(item.importance())
                    ))
                    .toList();
            return new MemoryExtractionResultDTO(items, items.isEmpty() ? "memory extraction empty" : "memory extraction completed");
        } catch (Exception exception) {
            log.warn("记忆提取失败，返回空结果", exception);
            return new MemoryExtractionResultDTO(Collections.emptyList(), "memory extraction empty");
        }
    }

    private String collectResponse(Flux<ChatResponse> stream) {
        StringBuilder responseText = new StringBuilder();
        stream.doOnNext(response -> {
            String text = extractTextFromResponse(response);
            if (text != null) {
                responseText.append(text);
            }
        }).blockLast();
        return responseText.toString().trim();
    }

    private String extractTextFromResponse(ChatResponse response) {
        if (response == null || response.getContent() == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (var block : response.getContent()) {
            if (block instanceof TextBlock textBlock) {
                builder.append(textBlock.getText());
            }
        }
        return builder.isEmpty() ? null : builder.toString();
    }

    private String unwrapMarkdownCodeBlock(String content) {
        if (content == null) {
            return "";
        }
        String normalized = content.trim();
        if (!normalized.startsWith("```")) {
            return normalized;
        }
        return normalized.replaceFirst("^```\\w*\\s*", "").replaceFirst("\\s*```$", "").trim();
    }

    private BigDecimal normalizeImportance(Double importance) {
        BigDecimal value = importance == null ? new BigDecimal("0.800") : BigDecimal.valueOf(importance);
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    ModelConfigEntity findModelByNameIgnoreCase(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return null;
        }
        ModelConfigEntity directMatch = modelConfigInfra.getByName(modelName);
        if (directMatch != null) {
            return directMatch;
        }
        return modelConfigInfra.lambdaQuery()
                .apply("LOWER(name) = LOWER({0})", modelName.trim())
                .last("LIMIT 1")
                .one();
    }

    private String buildMemoryKey(Long sessionId, String text) {
        String sessionPart = sessionId == null ? "global" : String.valueOf(sessionId);
        return sessionPart + "-extract-" + Integer.toHexString(text.trim().hashCode());
    }

    private String classifyMemoryType(String text) {
        String normalized = text == null ? "" : text.toLowerCase();
        if (normalized.contains("工具") || normalized.contains("命令") || normalized.contains("配置")) {
            return MemoryConstants.MEMORY_TYPE_TOOL_CONFIG;
        }
        if (normalized.contains("喜欢") || normalized.contains("偏好") || normalized.contains("习惯")) {
            return MemoryConstants.MEMORY_TYPE_PREFERENCE;
        }
        return MemoryConstants.MEMORY_TYPE_FACT;
    }

    private record ExtractionItem(String text, Double importance) {
    }
}
