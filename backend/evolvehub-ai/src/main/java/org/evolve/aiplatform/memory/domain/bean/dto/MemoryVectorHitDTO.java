package org.evolve.aiplatform.memory.domain.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 向量检索命中结果
 *
 * @author TellyJiang
 * @since 2026-04-15
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryVectorHitDTO {

    /*
     * 向量文档 ID
     */
    private String vectorDocId;

    /*
     * 命中文本内容
     */
    private String content;

    /*
     * 相似度分数
     */
    private BigDecimal score;

    /*
     * 重要性
     */
    private BigDecimal importance;

    /*
     * 向量载荷中的记忆归类
     */
    private String memoryKind;

    /*
     * 向量载荷中的摘要结束回合
     */
    private Integer roundEndNo;

}
