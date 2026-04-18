package org.evolve.aiplatform.memory.framework.agentscope.algorithm;

import io.agentscope.core.message.Msg;
import org.evolve.aiplatform.memory.domain.bean.entity.AgentMemoryRecordEntity;
import org.evolve.aiplatform.memory.domain.bean.vo.MemoryRecallResultVO;

import java.util.List;

/**
 * 记忆召回算法接口
 *
 * @author TellyJiang
 * @since 2026-04-14
 */
public interface MemoryRecallAlgorithm {

    /**
     * 整理召回结果
     *
     * @param queryMessage 查询消息
     * @param records 原始记录
     * @return 召回结果
     * @author TellyJiang
     * @since 2026-04-14
     */
    List<MemoryRecallResultVO> recall(Msg queryMessage, List<AgentMemoryRecordEntity> records);
}
