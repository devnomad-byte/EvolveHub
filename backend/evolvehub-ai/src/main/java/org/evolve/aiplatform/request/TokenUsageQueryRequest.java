package org.evolve.aiplatform.request;

import java.time.LocalDate;

/**
 * Token 消费统计查询请求
 *
 * @param startDate 起始日期（含），默认当月 1 号
 * @param endDate   结束日期（含），默认今天
 */
public record TokenUsageQueryRequest(
        LocalDate startDate,
        LocalDate endDate) {

    /**
     * 紧凑构造器：空值时设置默认日期范围（当月）
     */
    public TokenUsageQueryRequest {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
    }
}
