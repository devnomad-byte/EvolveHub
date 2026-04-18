package org.evolve.aiplatform.memory.domain.bean.vo;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 长期记忆召回结果
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryRecallResultVO {

    /*
     * 记忆键
     */
    private String memoryKey;

    /*
     * 记忆类型
     */
    private String memoryType;

    /*
     * 记忆内容
     */
    private String content;

    /*
     * 召回分数
     */
    private BigDecimal score;

    /*
     * 重要性
     */
    private BigDecimal importance;

    /*
     * 记忆归类
     */
    private String memoryKind;

    /*
     * 摘要覆盖结束回合
     */
    private Integer roundEndNo;

}
