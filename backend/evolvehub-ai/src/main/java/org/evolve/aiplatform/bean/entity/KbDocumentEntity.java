package org.evolve.aiplatform.bean.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 知识库文档实体
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Getter
@Setter
@TableName("eh_kb_document")
public class KbDocumentEntity extends BaseEntity {

    /**
     * 所属知识库 ID
     */
    private Long kbId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 切片总数
     */
    private Integer chunkCount;

    /**
     * 处理状态（0-待处理 1-处理中 2-已完成 3-失败）
     */
    private Integer status;

    /**
     * 失败时的错误信息
     */
    private String errorMessage;
}