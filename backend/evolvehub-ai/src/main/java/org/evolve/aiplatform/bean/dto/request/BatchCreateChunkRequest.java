package org.evolve.aiplatform.bean.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 批量创建切片请求
 *
 * @param docId  文档 ID
 * @param kbId   知识库 ID
 * @param chunks 切片列表
 */
public record BatchCreateChunkRequest(
        @NotNull(message = "文档ID不能为空") Long docId,
        @NotNull(message = "知识库ID不能为空") Long kbId,
        @NotEmpty(message = "切片列表不能为空") List<ChunkItem> chunks) {

    /**
     * 单个切片数据
     *
     * @param chunkIndex  切片索引（从 0 开始）
     * @param content     文本内容
     * @param tokenCount  Token 数量
     * @param pageNum     原始页码
     * @param headingPath 标题层级路径
     * @param chunkType   切片类型（text / table / image_desc）
     */
    public record ChunkItem(
            int chunkIndex,
            String content,
            int tokenCount,
            Integer pageNum,
            String headingPath,
            String chunkType) {
    }
}