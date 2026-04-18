package org.evolve.aiplatform.memory.domain.bean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * AgentScope Memory 对象实体
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Getter
@Setter
@TableName("eh_agent_memory_object")
public class AgentMemoryObjectEntity extends BaseEntity {

    /*
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /*
     * 会话 ID
     */
    @TableField("session_id")
    private String sessionId;

    /*
     * 对象类型
     */
    @TableField("object_type")
    private String objectType;

    /*
     * Bucket 名称
     */
    @TableField("bucket")
    private String bucket;

    /*
     * 对象键
     */
    @TableField("object_key")
    private String objectKey;

    /*
     * 内容类型
     */
    @TableField("content_type")
    private String contentType;

    /*
     * 校验码
     */
    @TableField("checksum")
    private String checksum;

    /*
     * 文件大小
     */
    @TableField("size_bytes")
    private Long sizeBytes;

    /*
     * 版本号
     */
    @TableField("version_no")
    private Integer versionNo;

    /*
     * 源记录 ID
     */
    @TableField("source_record_id")
    private Long sourceRecordId;
}
