package org.evolve.aiplatform.memory.domain.bean.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 会话记忆缓存对象
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemorySessionDTO {

    /*
     * 用户 ID
     */
    private Long userId;

    /*
     * 会话 ID
     */
    private String sessionId;

    /*
     * 模型名称
     */
    private String modelName;

    /*
     * 活动摘要
     */
    private List<MemoryActiveSummaryDTO> activeSummaries;

    /*
     * 最近回合列表
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
