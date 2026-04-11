package org.evolve.aiplatform.service;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.bean.dto.request.BatchCreateChunkRequest;
import org.evolve.aiplatform.bean.dto.response.BatchCreateChunkResponse;
import org.evolve.aiplatform.bean.entity.KbChunkEntity;
import org.evolve.aiplatform.bean.entity.KbDocumentEntity;
import org.evolve.aiplatform.mapper.KbChunkInfra;
import org.evolve.aiplatform.mapper.KbDocumentInfra;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量创建切片业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>文档必须存在</li>
 *     <li>切片创建完成后更新文档的切片总数</li>
 *     <li>切片初始状态为待向量化（status=0）</li>
 * </ul>
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Service
public class BatchCreateChunkManager extends BaseManager<BatchCreateChunkRequest, BatchCreateChunkResponse> {

    @Resource
    private KbDocumentInfra kbDocumentInfra;

    @Resource
    private KbChunkInfra kbChunkInfra;

    @Override
    protected void check(BatchCreateChunkRequest request) {
        KbDocumentEntity doc = kbDocumentInfra.getDocById(request.docId());
        if (doc == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "文档不存在");
        }
        if (!doc.getKbId().equals(request.kbId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文档不属于指定知识库");
        }
    }

    @Override
    protected BatchCreateChunkResponse process(BatchCreateChunkRequest request) {
        LocalDateTime now = LocalDateTime.now();
        List<KbChunkEntity> chunks = request.chunks().stream()
                .map(item -> {
                    KbChunkEntity chunk = new KbChunkEntity();
                    chunk.setDocId(request.docId());
                    chunk.setKbId(request.kbId());
                    chunk.setChunkIndex(item.chunkIndex());
                    chunk.setContent(item.content());
                    chunk.setTokenCount(item.tokenCount());
                    chunk.setPageNum(item.pageNum());
                    chunk.setHeadingPath(item.headingPath());
                    chunk.setChunkType(item.chunkType() != null ? item.chunkType() : "text");
                    chunk.setStatus(0);
                    chunk.setCreateTime(now);
                    return chunk;
                })
                .toList();

        kbChunkInfra.batchCreateChunks(chunks);

        // 更新文档切片总数
        KbDocumentEntity docUpdate = new KbDocumentEntity();
        docUpdate.setId(request.docId());
        docUpdate.setChunkCount((int) kbChunkInfra.countByDocId(request.docId()));
        kbDocumentInfra.updateDoc(docUpdate);

        return new BatchCreateChunkResponse(chunks.size());
    }
}