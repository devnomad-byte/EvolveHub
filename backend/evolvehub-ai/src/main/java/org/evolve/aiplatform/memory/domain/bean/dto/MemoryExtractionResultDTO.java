package org.evolve.aiplatform.memory.domain.bean.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 记忆提取结果对象
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryExtractionResultDTO {

    /*
     * 提取出的结构化记忆
     */
    private List<MemoryStructuredItemDTO> extractedItems;

    /*
     * 提取摘要
     */
    private String summary;

}
