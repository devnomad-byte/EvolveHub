package org.evolve.aiplatform.memory.domain.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;

import java.util.List;

/**
 * 会话上下文装配结果
 *
 * @author TellyJiang
 * @since 2026-04-16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryConversationContextDTO {

    /*
     * 用户画像 Markdown
     */
    private String profileMarkdown;

    /*
     * 活动摘要列表
     */
    private List<MemoryActiveSummaryDTO> activeSummaries;

    /*
     * 最近问答回合
     */
    private List<MemoryRoundDTO> recentRounds;

    /*
     * 当前查询召回的长期记忆
     */
    private List<MemoryRecallResultVO> recalledMemories;
}
