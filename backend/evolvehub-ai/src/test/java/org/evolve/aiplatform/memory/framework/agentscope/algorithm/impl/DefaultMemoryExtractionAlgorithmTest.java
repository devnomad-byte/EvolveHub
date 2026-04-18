package org.evolve.aiplatform.memory.framework.agentscope.algorithm.impl;

import org.evolve.aiplatform.config.ChatModelFactory;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionRequestDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryExtractionResultDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 默认记忆提取算法测试
 *
 * @author TellyJiang
 * @since 2026-04-15
 */
class DefaultMemoryExtractionAlgorithmTest {

    @Test
    void shouldIgnoreInvalidJsonResponse() {
        DefaultMemoryExtractionAlgorithm algorithm = buildAlgorithm("not-json");

        MemoryExtractionResultDTO result = algorithm.extract(new MemoryExtractionRequestDTO(1L, 2L, "session-model", "用户说：喜欢咖啡"));

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.getExtractedItems().isEmpty());
        Assertions.assertEquals("memory extraction empty", result.getSummary());
    }

    @Test
    void shouldParseMarkdownWrappedJsonAndFilterLowImportanceItems() {
        String response = """
                ```json
                [
                  {"text":"用户喜欢咖啡","importance":0.8},
                  {"text":"用户今天问了天气","importance":0.2}
                ]
                ```
                """;
        DefaultMemoryExtractionAlgorithm algorithm = buildAlgorithm(response);

        MemoryExtractionResultDTO result = algorithm.extract(new MemoryExtractionRequestDTO(
                1L,
                2L,
                "session-model",
                "用户说：我喜欢咖啡。助手回复：收到。"
        ));

        Assertions.assertEquals(1, result.getExtractedItems().size());
        MemoryStructuredItemDTO item = result.getExtractedItems().get(0);
        Assertions.assertEquals("用户喜欢咖啡", item.getContent());
        Assertions.assertEquals(new BigDecimal("0.800"), item.getImportance());
        Assertions.assertEquals("memory extraction completed", result.getSummary());
    }

    @Test
    void shouldUseModelNameFromRequestFirst() {
        DefaultMemoryExtractionAlgorithm algorithm = Mockito.spy(new DefaultMemoryExtractionAlgorithm());
        ChatModelFactory chatModelFactory = Mockito.mock(ChatModelFactory.class);
        ModelConfigEntity modelConfig = new ModelConfigEntity();
        modelConfig.setName("session-model");

        Mockito.when(chatModelFactory.createModel(modelConfig)).thenReturn(new StubChatModel("""
                [{"text":"用户喜欢咖啡","importance":0.8}]
                """));
        Mockito.doReturn(modelConfig).when(algorithm).findModelByNameIgnoreCase("session-model");

        ReflectionTestUtils.setField(algorithm, "chatModelFactory", chatModelFactory);
        ReflectionTestUtils.setField(algorithm, "objectMapper", new com.fasterxml.jackson.databind.ObjectMapper());

        MemoryExtractionResultDTO result = algorithm.extract(new MemoryExtractionRequestDTO(1L, 2L, "session-model", "用户说：我喜欢咖啡"));

        Assertions.assertEquals(1, result.getExtractedItems().size());
        Mockito.verify(algorithm).findModelByNameIgnoreCase("session-model");
        Mockito.verify(algorithm, Mockito.never()).findModelByNameIgnoreCase("memory-extractor");
        Mockito.verify(algorithm, Mockito.never()).findModelByNameIgnoreCase("gpt-4o-mini");
    }

    private DefaultMemoryExtractionAlgorithm buildAlgorithm(String modelOutput) {
        DefaultMemoryExtractionAlgorithm algorithm = Mockito.spy(new DefaultMemoryExtractionAlgorithm());
        ChatModelFactory chatModelFactory = Mockito.mock(ChatModelFactory.class);
        ModelConfigEntity modelConfig = new ModelConfigEntity();
        modelConfig.setName("session-model");
        Mockito.when(chatModelFactory.createModel(modelConfig)).thenReturn(new StubChatModel(modelOutput));
        Mockito.doReturn(modelConfig).when(algorithm).findModelByNameIgnoreCase("session-model");
        ReflectionTestUtils.setField(algorithm, "chatModelFactory", chatModelFactory);
        ReflectionTestUtils.setField(algorithm, "objectMapper", new com.fasterxml.jackson.databind.ObjectMapper());
        return algorithm;
    }

    private static final class StubChatModel extends io.agentscope.core.model.ChatModelBase {

        private final String output;

        private StubChatModel(String output) {
            this.output = output;
        }

        @Override
        protected reactor.core.publisher.Flux<io.agentscope.core.model.ChatResponse> doStream(
                List<io.agentscope.core.message.Msg> messages,
                List<io.agentscope.core.model.ToolSchema> toolSchemas,
                io.agentscope.core.model.GenerateOptions generateOptions) {
            io.agentscope.core.model.ChatResponse response = io.agentscope.core.model.ChatResponse.builder()
                    .content(List.of(io.agentscope.core.message.TextBlock.builder().text(output).build()))
                    .build();
            return reactor.core.publisher.Flux.just(response);
        }

        @Override
        public String getModelName() {
            return "stub-model";
        }

        public String getProvider() {
            return "stub";
        }
    }
}
