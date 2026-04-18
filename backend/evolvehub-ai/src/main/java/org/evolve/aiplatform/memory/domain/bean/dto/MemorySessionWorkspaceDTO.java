package org.evolve.aiplatform.memory.domain.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话工作区对象
 *
 * @author TellyJiang
 * @since 2026-04-16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemorySessionWorkspaceDTO {

    /*
     * 用户 ID
     */
    private Long userId;

    /*
     * 会话 ID
     */
    private String sessionId;

    /*
     * 当前模型名称
     */
    private String modelName;

    /*
     * 当前最新回合号
     */
    private Integer currentRoundNo;

    /*
     * 已压缩到的回合号
     */
    private Integer lastCompactedRoundNo;

    /*
     * 活动摘要
     */
    private List<MemoryActiveSummaryDTO> activeSummaries;

    /*
     * 最近问答回合
     */
    private List<MemoryRoundDTO> rounds;

    /*
     * 创建时间
     */
    private LocalDateTime createdAt;

    /*
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
