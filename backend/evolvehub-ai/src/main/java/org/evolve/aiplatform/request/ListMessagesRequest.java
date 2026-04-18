package org.evolve.aiplatform.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ListMessagesRequest(
        @NotNull(message = "会话 ID 不能为空") Long sessionId,
        @Min(value = 1, message = "页码最小为1") int pageNum,
        @Min(value = 1, message = "每页条数最小为1") int pageSize) {

    public ListMessagesRequest {
        if (pageNum == 0) pageNum = 1;
        if (pageSize == 0) pageSize = 20;
    }
}
