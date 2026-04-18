package org.evolve.aiplatform.memory.domain.bean.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 结构化记忆数据传输对象
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryStructuredItemDTO {

    /*
     * 用户 ID
     */
    private Long userId;

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
     * 重要性
     */
    private BigDecimal importance;

}
