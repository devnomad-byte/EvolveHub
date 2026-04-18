package org.evolve.aiplatform.memory.domain.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 可管理记忆返回对象
 *
 * @author TellyJiang
 * @since 2026-04-15
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryManagedItemVO {

    /*
     * 主键 ID
     */
    private Long id;

    /*
     * 向量文档 ID
     */
    private String vectorDocId;

    /*
     * 记忆键
     */
    private String memoryKey;

    /*
     * 记忆类型
     */
    private String memoryType;

    /*
     * 来源类型
     */
    private String sourceKind;

    /*
     * 记忆内容
     */
    private String content;

    /*
     * 重要性
     */
    private BigDecimal importance;

    /*
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
