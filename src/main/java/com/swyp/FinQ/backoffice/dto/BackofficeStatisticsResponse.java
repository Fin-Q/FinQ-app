package com.swyp.FinQ.backoffice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BackofficeStatisticsResponse(
        long usersBeforeRange,
        List<Statistic> statistics,
        List<Signup> signups
) {
    public record Statistic(
            LocalDate date,
            long learnedUsers,
            long streak3Users,
            long streak7Users,
            LocalDateTime calculatedAt
    ) {
    }

    public record Signup(LocalDate date, long count) {
    }
}
