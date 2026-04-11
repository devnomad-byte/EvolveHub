package org.evolve.aiplatform.bean.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 上传文档请求
 *
 * @param kbId     所属知识库 ID
 * @param fileName 文件名称
 * @param filePath 文件存储路径
 * @param fileSize 文件大小（字节）
 */
public record UploadDocRequest(
        @NotNull(message = "知识库ID不能为空") Long kbId,
        @NotBlank(message = "文件名不能为空") String fileName,
        @NotBlank(message = "文件路径不能为空") String filePath,
        @NotNull(message = "文件大小不能为空") Long fileSize) {
}