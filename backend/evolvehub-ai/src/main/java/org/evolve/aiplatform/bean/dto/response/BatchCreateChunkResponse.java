package org.evolve.aiplatform.bean.dto.response;

/** 批量创建切片响应（返回成功入库的切片数量） */
public record BatchCreateChunkResponse(int savedCount) {}