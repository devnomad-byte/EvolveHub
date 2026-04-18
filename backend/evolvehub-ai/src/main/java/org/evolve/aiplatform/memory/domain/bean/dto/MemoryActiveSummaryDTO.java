package org.evolve.aiplatform.memory.domain.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 活动记忆摘要对象
 *
 * @author TellyJiang
 * @since 2026-04-16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryActiveSummaryDTO {

    /*
     * 摘要标识
     */
    private String summaryKey;

    /*
     * 摘要内容
     */
    private String content;

    /*
     * 最近命中回合
     */
    private Integer lastActivatedRoundNo;

    /*
     * 沉睡回合阈值
     */
    private Integer sleepAfterRoundNo;
}
