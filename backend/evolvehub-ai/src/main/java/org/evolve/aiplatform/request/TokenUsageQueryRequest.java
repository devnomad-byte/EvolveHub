package org.evolve.aiplatform.request;

import java.time.LocalDate;

public record TokenUsageQueryRequest(
        LocalDate startDate,
        LocalDate endDate) {

    public TokenUsageQueryRequest {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();
    }
}
