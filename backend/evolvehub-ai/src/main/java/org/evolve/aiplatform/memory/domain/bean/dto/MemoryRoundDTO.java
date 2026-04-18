package org.evolve.aiplatform.memory.domain.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 会话问答回合对象
 *
 * @author TellyJiang
 * @since 2026-04-16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryRoundDTO {

    /*
     * 回合序号
     */
    private Integer roundNo;

    /*
     * 用户消息
     */
    private String userMessage;

    /*
     * 助手回复
     */
    private String assistantMessage;

    /*
     * 模型名称
     */
    private String modelName;

    /*
     * 产生时间
     */
    private LocalDateTime occurredAt;
}
