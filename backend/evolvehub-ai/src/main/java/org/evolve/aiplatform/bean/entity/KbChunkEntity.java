package org.evolve.aiplatform.bean.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库切片实体
 * <p>
 * 切片不需要逻辑删除和审计字段，直接实现 Serializable，
 * 主键字段名为 chunk_id 而非通用的 id，故不继承 BaseEntity。
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Getter
@Setter
@TableName("eh_kb_chunk")
public class KbChunkEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 切片主键 ID（雪花算法）
     */
    @TableId(value = "chunk_id", type = IdType.ASSIGN_ID)
    private Long chunkId;

    /**
     * 所属文档 ID
     */
    private Long docId;

    /**
     * 所属知识库 ID（冗余，便于按库检索）
     */
    private Long kbId;

    /**
     * 切片在文档内的顺序索引（从 0 开始）
     */
    private Integer chunkIndex;

    /**
     * 切片文本内容
     */
    private String content;

    /**
     * Token 数量
     */
    private Integer tokenCount;

    /**
     * 原始页码（PDF 等有页概念的文档）
     */
    private Integer pageNum;

    /**
     * 标题层级路径（如 "第一章 > 1.1 概述"）
     */
    private String headingPath;

    /**
     * 切片类型（text / table / image_desc）
     */
    private String chunkType;

    /**
     * 向量化状态（0-待向量化 1-向量化中 2-已入库 3-失败）
     */
    private Integer status;

    /**
     * Milvus 向量库中的记录 ID
     */
    private String milvusId;

    /**
     * 失败时的错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}