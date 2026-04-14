package org.evolve.aiplatform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.DocumentMetadata;
import io.agentscope.core.rag.store.MilvusStore;
import io.agentscope.core.rag.store.dto.SearchDocumentDto;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.config.ChatModelFactory;
import org.evolve.aiplatform.config.EmbeddingService;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 长期记忆服务
 * <p>
 * 基于 Milvus（Zilliz Cloud）实现长期记忆的存储、检索和管理。
 * <ul>
 *   <li>检索：将查询文本向量化 → Milvus 余弦相似度搜索 → 返回 Top-K 记忆文本</li>
 *   <li>提取：对话结束后，用 LLM 提取值得记忆的内容 → 向量化 → 存入 Milvus</li>
 *   <li>去重：新记忆写入前检索相似度 > 0.95 的已有记忆，避免重复</li>
 * </ul>
 * </p>
 *
 * @author zhao
 */
@Service
public class ChatMemoryService {

    private static final Logger log = LoggerFactory.getLogger(ChatMemoryService.class);

    /**
     * 记忆检索默认返回条数
     */
    private static final int DEFAULT_TOP_K = 5;

    /**
     * 去重相似度阈值（> 0.95 视为重复）
     */
    private static final double DEDUP_THRESHOLD = 0.95;

    /**
     * 记忆重要性评分最低阈值（< 0.5 的不写入向量库）
     */
    private static final double IMPORTANCE_THRESHOLD = 0.5;

    /**
     * 记忆提取的系统提示词（含重要性评分）
     */
    private static final String EXTRACTION_PROMPT = """
            你是一个记忆提取助手。分析以下对话，提取用户透露的**值得长期记住**的信息。
            提取规则：
            1. 只提取用户的偏好、习惯、个人信息、重要决策、专业知识等
            2. 不要提取临时性、一次性的对话内容
            3. 每条记忆用一句简洁的陈述句表达
            4. 为每条记忆评一个重要性分数（0.0~1.0），0.0 表示无关紧要，1.0 表示非常重要
            5. 如果没有值得记忆的内容，返回空数组 []
            
            以 JSON 数组格式返回，每个元素包含 text 和 importance 字段，例如：
            [{"text": "用户喜欢用 Java 21", "importance": 0.8}, {"text": "用户的项目名叫 EvolveHub", "importance": 0.9}]
            
            只返回 JSON 数组，不要其他内容。
            """;

    /**
     * 异步执行记忆提取的线程池
     */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Resource
    private MilvusStore milvusStore;

    @Resource
    private EmbeddingService embeddingService;

    @Resource
    private ChatModelFactory chatModelFactory;

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 检索与查询文本最相关的长期记忆（使用原生 MilvusClientV2，支持 user_id filter）
     *
     * @param userId 用户 ID
     * @param query  查询文本
     * @param topK   返回条数
     * @return 记忆文本列表（按相关性排序）
     */
    public List<String> retrieve(Long userId, String query, int topK) {
        try {
            double[] queryEmbedding = embeddingService.embedSync(query);
            float[] floatVec = toFloatArray(queryEmbedding);

            MilvusClientV2 client = milvusStore.getClient();
            SearchReq searchReq = SearchReq.builder()
                    .collectionName(milvusStore.getCollectionName())
                    .data(List.of(new FloatVec(floatVec)))
                    .topK(topK > 0 ? topK : DEFAULT_TOP_K)
                    .filter("payload[\"user_id\"] == " + userId)
                    .outputFields(List.of("content", "payload", "doc_id"))
                    .build();
            SearchResp resp = client.search(searchReq);

            List<List<SearchResp.SearchResult>> searchResults = resp.getSearchResults();
            if (searchResults == null || searchResults.isEmpty() || searchResults.get(0).isEmpty()) {
                return Collections.emptyList();
            }
            List<String> memories = new ArrayList<>();
            List<String> hitDocIds = new ArrayList<>();
            for (SearchResp.SearchResult result : searchResults.get(0)) {
                Object contentObj = result.getEntity().get("content");
                if (contentObj != null) {
                    String text = contentObj.toString();
                    if (!text.isBlank()) {
                        memories.add(text);
                        // 收集命中的 doc_id 用于 access_count 更新
                        Object docIdObj = result.getEntity().get("doc_id");
                        if (docIdObj != null) {
                            hitDocIds.add(docIdObj.toString());
                        }
                    }
                }
            }
            // 异步更新命中记忆的 access_count
            if (!hitDocIds.isEmpty()) {
                executor.execute(() -> incrementAccessCount(hitDocIds));
            }
            return memories;
        } catch (Exception e) {
            log.error("记忆检索失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 检索记忆（使用默认 Top-K）
     */
    public List<String> retrieve(Long userId, String query) {
        return retrieve(userId, query, DEFAULT_TOP_K);
    }

    /**
     * 异步增加命中记忆的 access_count（热度追踪）
     *
     * @param docIds 命中的文档 ID 列表
     */
    private void incrementAccessCount(List<String> docIds) {
        try {
            MilvusClientV2 client = milvusStore.getClient();
            String collectionName = milvusStore.getCollectionName();

            // 先查询当前的完整记录
            String filter = "doc_id in [" + docIds.stream().map(id -> "\"" + id + "\"").reduce((a, b) -> a + "," + b).orElse("") + "]";
            QueryReq queryReq = QueryReq.builder()
                    .collectionName(collectionName)
                    .filter(filter)
                    .outputFields(List.of("doc_id", "content", "payload", "embedding"))
                    .limit(docIds.size())
                    .build();
            QueryResp resp = client.query(queryReq);

            if (resp.getQueryResults() == null || resp.getQueryResults().isEmpty()) {
                return;
            }

            Gson gson = new Gson();
            for (QueryResp.QueryResult result : resp.getQueryResults()) {
                Map<String, Object> entity = result.getEntity();
                Object payloadObj = entity.get("payload");
                if (payloadObj == null) continue;

                // 解析 payload 并更新 access_count
                JsonObject payload = gson.fromJson(gson.toJson(payloadObj), JsonObject.class);
                int currentCount = payload.has("access_count") ? payload.get("access_count").getAsInt() : 0;
                payload.addProperty("access_count", currentCount + 1);

                // 通过 upsert 更新记录
                JsonObject row = new JsonObject();
                row.addProperty("doc_id", entity.get("doc_id").toString());
                row.addProperty("content", entity.get("content").toString());
                row.add("payload", payload);
                // embedding 保持不变
                Object embObj = entity.get("embedding");
                if (embObj != null) {
                    row.add("embedding", gson.toJsonTree(embObj));
                }

                UpsertReq upsertReq = UpsertReq.builder()
                        .collectionName(collectionName)
                        .data(List.of(row))
                        .build();
                client.upsert(upsertReq);
            }
            log.debug("access_count 已更新: docIds={}", docIds);
        } catch (Exception e) {
            log.warn("access_count 更新失败", e);
        }
    }

    /**
     * 异步提取并保存记忆（对话结束后调用）
     *
     * @param userId       用户 ID
     * @param sessionId    会话 ID
     * @param modelConfig  对话使用的模型配置
     * @param userMessage  用户消息
     * @param assistantMessage 助手回复
     */
    public void extractAndSaveAsync(Long userId, Long sessionId, ModelConfigEntity modelConfig,
                                     String userMessage, String assistantMessage) {
        executor.execute(() -> {
            try {
                extractAndSave(userId, sessionId, modelConfig, userMessage, assistantMessage);
            } catch (Exception e) {
                log.error("记忆提取失败: userId={}, sessionId={}", userId, sessionId, e);
            }
        });
    }

    /**
     * 同步提取并保存记忆（含重要性评分过滤）
     */
    private void extractAndSave(Long userId, Long sessionId, ModelConfigEntity modelConfig,
                                 String userMessage, String assistantMessage) {
        // 1. 用 LLM 提取记忆（含重要性评分）
        List<MemoryItem> memories = extractMemories(modelConfig, userMessage, assistantMessage);
        if (memories.isEmpty()) {
            log.debug("本轮对话未提取到需要记忆的内容: sessionId={}", sessionId);
            return;
        }

        // 2. 过滤低重要性记忆 + 逐条去重并保存
        for (MemoryItem item : memories) {
            if (item.importance() < IMPORTANCE_THRESHOLD) {
                log.debug("记忆重要性低于阈值，跳过: text={}, importance={}", item.text(), item.importance());
                continue;
            }
            try {
                saveMemoryWithDedup(userId, sessionId, item.text(), item.importance());
            } catch (Exception e) {
                log.error("保存单条记忆失败: text={}", item.text(), e);
            }
        }
    }

    /**
     * 记忆提取项（文本 + 重要性评分）
     */
    private record MemoryItem(String text, double importance) {}

    /**
     * 调用 LLM 从对话中提取值得记忆的内容（含重要性评分）
     */
    private List<MemoryItem> extractMemories(ModelConfigEntity modelConfig, String userMessage, String assistantMessage) {
        try {
            ChatModelBase model = chatModelFactory.createModel(modelConfig);
            List<Msg> messages = List.of(
                    Msg.builder().role(MsgRole.SYSTEM).textContent(EXTRACTION_PROMPT).build(),
                    Msg.builder().role(MsgRole.USER).textContent(
                            "用户说：" + userMessage + "\n\n助手回复：" + assistantMessage
                    ).build()
            );

            // 同步调用模型（非流式）
            StringBuilder responseText = new StringBuilder();
            model.stream(messages, null, null)
                    .doOnNext(response -> {
                        String text = extractTextFromResponse(response);
                        if (text != null) {
                            responseText.append(text);
                        }
                    })
                    .blockLast();

            String jsonStr = responseText.toString().trim();
            // 去掉可能的 markdown 代码块标记
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.replaceAll("^```\\w*\\n?", "").replaceAll("\\n?```$", "");
            }
            return objectMapper.readValue(jsonStr, new TypeReference<List<MemoryItem>>() {});
        } catch (Exception e) {
            log.warn("LLM 记忆提取解析失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 去重后保存单条记忆到 Milvus（含重要性评分）
     *
     * @param userId     用户 ID
     * @param sessionId  会话 ID
     * @param memoryText 记忆内容
     * @param importance 重要性评分（0.0~1.0）
     */
    public void saveMemoryWithDedup(Long userId, Long sessionId, String memoryText, double importance) {
        double[] embedding = embeddingService.embedSync(memoryText);
        float[] floatVec = toFloatArray(embedding);

        // 检查是否已有高度相似的记忆（按 user_id 过滤）
        try {
            MilvusClientV2 client = milvusStore.getClient();
            SearchReq searchReq = SearchReq.builder()
                    .collectionName(milvusStore.getCollectionName())
                    .data(List.of(new FloatVec(floatVec)))
                    .topK(1)
                    .filter("payload[\"user_id\"] == " + userId)
                    .outputFields(List.of("content", "doc_id"))
                    .build();
            SearchResp resp = client.search(searchReq);
            List<List<SearchResp.SearchResult>> results = resp.getSearchResults();
            if (results != null && !results.isEmpty() && !results.get(0).isEmpty()) {
                SearchResp.SearchResult top = results.get(0).get(0);
                if (top.getScore() >= DEDUP_THRESHOLD) {
                    log.debug("记忆去重命中(score={})，跳过保存: text={}", top.getScore(), memoryText);
                    return;
                }
            }
        } catch (Exception e) {
            log.warn("记忆去重检索失败，继续保存: text={}", memoryText, e);
        }

        // 构建 Document 并写入
        DocumentMetadata metadata = DocumentMetadata.builder()
                .content(TextBlock.builder().text(memoryText).build())
                .docId(UUID.randomUUID().toString())
                .chunkId("0")
                .addPayload("user_id", userId)
                .addPayload("session_id", sessionId)
                .addPayload("category", "auto_extracted")
                .addPayload("importance", importance)
                .addPayload("access_count", 0)
                .build();
        Document doc = new Document(metadata);
        doc.setEmbedding(embedding);
        milvusStore.add(List.of(doc)).block();
        log.info("保存新记忆: userId={}, importance={}, text={}", userId, importance, memoryText);
    }

    /**
     * 去重后保存单条记忆（默认重要性 0.8，供外部工具调用）
     */
    public void saveMemoryWithDedup(Long userId, Long sessionId, String memoryText) {
        saveMemoryWithDedup(userId, sessionId, memoryText, 0.8);
    }

    /**
     * 查询用户的所有记忆（使用原生 Milvus Query，按 user_id 精确过滤，不依赖向量相似度）
     *
     * @param userId 用户 ID
     * @return 记忆文本列表
     */
    public List<String> listUserMemories(Long userId) {
        try {
            MilvusClientV2 client = milvusStore.getClient();
            QueryReq queryReq = QueryReq.builder()
                    .collectionName(milvusStore.getCollectionName())
                    .filter("payload[\"user_id\"] == " + userId)
                    .outputFields(List.of("content", "doc_id"))
                    .limit(100)
                    .build();
            QueryResp resp = client.query(queryReq);
            List<QueryResp.QueryResult> results = resp.getQueryResults();
            if (results == null || results.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> memories = new ArrayList<>();
            for (QueryResp.QueryResult result : results) {
                Object contentObj = result.getEntity().get("content");
                if (contentObj != null) {
                    String text = contentObj.toString();
                    if (!text.isBlank()) {
                        memories.add(text);
                    }
                }
            }
            return memories;
        } catch (Exception e) {
            log.error("查询用户记忆失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 删除一条记忆
     *
     * @param docId 文档 ID
     */
    public void deleteMemory(String docId) {
        Boolean deleted = milvusStore.delete(docId).block();
        if (Boolean.TRUE.equals(deleted)) {
            log.info("删除记忆成功: docId={}", docId);
        } else {
            throw new BusinessException("记忆不存在或删除失败");
        }
    }

    /**
     * 从 ChatResponse 中提取文本
     */
    private String extractTextFromResponse(ChatResponse response) {
        if (response.getContent() == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (var block : response.getContent()) {
            if (block instanceof TextBlock textBlock) {
                sb.append(textBlock.getText());
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    /**
     * double[] 转 float[]（Milvus SDK 需要 float[]）
     */
    private float[] toFloatArray(double[] doubles) {
        float[] floats = new float[doubles.length];
        for (int i = 0; i < doubles.length; i++) {
            floats[i] = (float) doubles[i];
        }
        return floats;
    }
}
