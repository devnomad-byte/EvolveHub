package org.evolve.aiplatform.memory.infrastructure.vector;

import com.google.gson.JsonObject;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.rag.model.Document;
import io.agentscope.core.rag.model.DocumentMetadata;
import io.agentscope.core.rag.store.MilvusStore;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.config.EmbeddingService;
import org.evolve.aiplatform.memory.application.service.MemoryVectorStore;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryVectorHitDTO;
import org.evolve.common.web.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 基于 Milvus 的向量存储实现
 *
 * @author TellyJiang
 * @since 2026-04-15
 */
@Component
public class MilvusMemoryVectorStore implements MemoryVectorStore {

    /**
     * 默认召回数量
     */
    private static final int DEFAULT_TOP_K = 5;

    /**
     * 相似度命中 access_count 字段
     */
    private static final String PAYLOAD_ACCESS_COUNT = "access_count";

    private static final Logger log = LoggerFactory.getLogger(MilvusMemoryVectorStore.class);

    @Resource
    private MilvusStore milvusStore;

    @Resource
    private EmbeddingService embeddingService;

    @Override
    public List<MemoryVectorHitDTO> search(Long userId, String query, Integer topK) {
        try {
            float[] queryEmbedding = toFloatArray(embeddingService.embedSync(query));
            MilvusClientV2 client = milvusStore.getClient();
            SearchReq searchReq = SearchReq.builder()
                    .collectionName(milvusStore.getCollectionName())
                    .data(List.of(new FloatVec(queryEmbedding)))
                    .topK(topK == null || topK <= 0 ? DEFAULT_TOP_K : topK)
                    .filter("payload[\"user_id\"] == " + userId)
                    .outputFields(List.of("content", "payload", "doc_id"))
                    .build();
            SearchResp response = client.search(searchReq);
            List<List<SearchResp.SearchResult>> results = response.getSearchResults();
            if (results == null || results.isEmpty() || results.get(0).isEmpty()) {
                return Collections.emptyList();
            }
            List<String> hitDocIds = new ArrayList<>();
            List<MemoryVectorHitDTO> recallResults = new ArrayList<>();
            for (SearchResp.SearchResult result : results.get(0)) {
                Object contentObject = result.getEntity().get("content");
                Object docIdObject = result.getEntity().get("doc_id");
                if (contentObject == null || docIdObject == null) {
                    continue;
                }
                hitDocIds.add(docIdObject.toString());
                recallResults.add(new MemoryVectorHitDTO(
                        docIdObject.toString(),
                        contentObject.toString(),
                        BigDecimal.valueOf(result.getScore()).setScale(3, RoundingMode.HALF_UP),
                        resolveImportance(result.getEntity().get("payload")),
                        resolvePayloadString(result.getEntity().get("payload"), "memory_kind"),
                        resolvePayloadInteger(result.getEntity().get("payload"), "round_end_no")
                ));
            }
            incrementAccessCount(hitDocIds);
            return recallResults;
        } catch (Exception exception) {
            log.error("Milvus 召回失败: userId={}", userId, exception);
            return Collections.emptyList();
        }
    }

    @Override
    public String save(Long userId, Long deptId, Long sessionId, String content, BigDecimal importance,
                       String memoryKind, Integer roundStartNo, Integer roundEndNo) {
        String vectorDocId = UUID.randomUUID().toString();
        DocumentMetadata metadata = DocumentMetadata.builder()
                .content(TextBlock.builder().text(content).build())
                .docId(vectorDocId)
                .chunkId("0")
                .addPayload("user_id", userId)
                .addPayload("dept_id", deptId)
                .addPayload("session_id", sessionId)
                .addPayload("importance", importance == null ? BigDecimal.ZERO.doubleValue() : importance.doubleValue())
                .addPayload("memory_kind", memoryKind)
                .addPayload("round_start_no", roundStartNo)
                .addPayload("round_end_no", roundEndNo)
                .addPayload(PAYLOAD_ACCESS_COUNT, 0)
                .build();
        Document document = new Document(metadata);
        document.setEmbedding(embeddingService.embedSync(content));
        milvusStore.add(List.of(document)).block();
        return vectorDocId;
    }

    @Override
    public boolean existsSimilar(Long userId, String content, double threshold) {
        try {
            float[] queryEmbedding = toFloatArray(embeddingService.embedSync(content));
            MilvusClientV2 client = milvusStore.getClient();
            SearchReq searchReq = SearchReq.builder()
                    .collectionName(milvusStore.getCollectionName())
                    .data(List.of(new FloatVec(queryEmbedding)))
                    .topK(1)
                    .filter("payload[\"user_id\"] == " + userId)
                    .outputFields(List.of("doc_id"))
                    .build();
            SearchResp response = client.search(searchReq);
            List<List<SearchResp.SearchResult>> results = response.getSearchResults();
            return results != null
                    && !results.isEmpty()
                    && !results.get(0).isEmpty()
                    && results.get(0).get(0).getScore() >= threshold;
        } catch (Exception exception) {
            log.warn("Milvus 相似度检索失败，默认视为不存在重复记忆: userId={}", userId, exception);
            return false;
        }
    }

    @Override
    public void delete(String vectorDocId) {
        Boolean deleted = milvusStore.delete(vectorDocId).block();
        if (!Boolean.TRUE.equals(deleted)) {
            throw new BusinessException("向量记忆不存在或删除失败");
        }
    }

    private void incrementAccessCount(List<String> hitDocIds) {
        if (hitDocIds == null || hitDocIds.isEmpty()) {
            return;
        }
        try {
            MilvusClientV2 client = milvusStore.getClient();
            QueryReq queryReq = QueryReq.builder()
                    .collectionName(milvusStore.getCollectionName())
                    .filter("doc_id in [" + hitDocIds.stream().map(id -> "\"" + id + "\"").reduce((left, right) -> left + "," + right).orElse("") + "]")
                    .outputFields(List.of("doc_id", "content", "payload", "embedding"))
                    .limit(hitDocIds.size())
                    .build();
            QueryResp response = client.query(queryReq);
            if (response.getQueryResults() == null || response.getQueryResults().isEmpty()) {
                return;
            }
            com.google.gson.Gson gson = new com.google.gson.Gson();
            for (QueryResp.QueryResult queryResult : response.getQueryResults()) {
                Object payloadObject = queryResult.getEntity().get("payload");
                if (payloadObject == null) {
                    continue;
                }
                JsonObject payload = gson.fromJson(gson.toJson(payloadObject), JsonObject.class);
                int currentCount = payload.has(PAYLOAD_ACCESS_COUNT) ? payload.get(PAYLOAD_ACCESS_COUNT).getAsInt() : 0;
                payload.addProperty(PAYLOAD_ACCESS_COUNT, currentCount + 1);
                JsonObject row = new JsonObject();
                row.addProperty("doc_id", queryResult.getEntity().get("doc_id").toString());
                row.addProperty("content", queryResult.getEntity().get("content").toString());
                row.add("payload", payload);
                Object embeddingObject = queryResult.getEntity().get("embedding");
                if (embeddingObject != null) {
                    row.add("embedding", gson.toJsonTree(embeddingObject));
                }
                UpsertReq upsertReq = UpsertReq.builder()
                        .collectionName(milvusStore.getCollectionName())
                        .data(List.of(row))
                        .build();
                client.upsert(upsertReq);
            }
        } catch (Exception exception) {
            log.warn("Milvus access_count 更新失败", exception);
        }
    }

    private BigDecimal resolveImportance(Object payloadObject) {
        if (payloadObject == null) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        com.google.gson.Gson gson = new com.google.gson.Gson();
        JsonObject payload = gson.fromJson(gson.toJson(payloadObject), JsonObject.class);
        if (!payload.has("importance")) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(payload.get("importance").getAsDouble()).setScale(3, RoundingMode.HALF_UP);
    }

    private String resolvePayloadString(Object payloadObject, String fieldName) {
        if (payloadObject == null) {
            return null;
        }
        com.google.gson.Gson gson = new com.google.gson.Gson();
        JsonObject payload = gson.fromJson(gson.toJson(payloadObject), JsonObject.class);
        return payload.has(fieldName) && !payload.get(fieldName).isJsonNull()
                ? payload.get(fieldName).getAsString()
                : null;
    }

    private Integer resolvePayloadInteger(Object payloadObject, String fieldName) {
        if (payloadObject == null) {
            return null;
        }
        com.google.gson.Gson gson = new com.google.gson.Gson();
        JsonObject payload = gson.fromJson(gson.toJson(payloadObject), JsonObject.class);
        return payload.has(fieldName) && !payload.get(fieldName).isJsonNull()
                ? payload.get(fieldName).getAsInt()
                : null;
    }

    private float[] toFloatArray(double[] values) {
        float[] result = new float[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (float) values[index];
        }
        return result;
    }
}
