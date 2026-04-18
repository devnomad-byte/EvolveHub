package org.evolve.aiplatform.memory.domain.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户画像记忆数据传输对象
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryProfileDTO {

    /*
     * 用户 ID
     */
    private Long userId;

    /*
     * 姓名
     */
    private String name;

    /*
     * 部门
     */
    private String department;

    /*
     * 语言偏好
     */
    private String language;

    /*
     * 偏好模型
     */
    private String preferredModel;

    /*
     * 工具偏好
     */
    private String toolPreference;

    /*
     * 当前 Markdown 内容
     */
    private String markdownContent;

}
