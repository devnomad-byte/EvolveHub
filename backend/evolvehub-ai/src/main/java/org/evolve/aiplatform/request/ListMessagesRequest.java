package org.evolve.aiplatform.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 查询会话消息列表请求
 *
 * @param sessionId 会话 ID
 * @param pageNum   页码，最小为 1
 * @param pageSize  每页条数，最小为 1
 */
public record ListMessagesRequest(
        @NotNull(message = "会话 ID 不能为空") Long sessionId,
        @Min(value = 1, message = "页码最小为1") int pageNum,
        @Min(value = 1, message = "每页条数最小为1") int pageSize) {

    /**
     * 紧凑构造器：当传入值为 0 时自动设置默认值
     */
    public ListMessagesRequest {
        if (pageNum == 0) pageNum = 1;
        if (pageSize == 0) pageSize = 20;
    }
}
