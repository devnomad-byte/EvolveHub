package org.evolve.aiplatform.memory.framework.agentscope.algorithm;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryStructuredItemDTO;

import java.util.List;

/**
 * 用户画像投影算法接口
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
public interface MemoryProfileProjectionAlgorithm {

    /**
     * 将画像投影为结构化记忆
     *
     * @param profile 用户画像
     * @return 结构化记忆列表
     * @author TellyJiang
     * @since 2026-04-14
     */
    List<MemoryStructuredItemDTO> projectStructuredItems(MemoryProfileDTO profile);

    /**
     * 构建画像摘要
     *
     * @param profile 用户画像
     * @return 摘要文本
     * @author TellyJiang
     * @since 2026-04-14
     */
    String buildProfileSummary(MemoryProfileDTO profile);
}
