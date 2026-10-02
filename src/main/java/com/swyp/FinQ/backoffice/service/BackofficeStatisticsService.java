package com.swyp.FinQ.backoffice.service;

import com.swyp.FinQ.backoffice.dto.BackofficeStatisticsResponse;
import com.swyp.FinQ.backoffice.repository.BackofficeStatisticsQueryRepository;
import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.global.exception.GlobalErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class BackofficeStatisticsService {

    private static final long MAX_RANGE_DAYS = 365;

    private final BackofficeStatisticsQueryRepository repository;
    @Qualifier("streakClock")
    private final Clock clock;

    public BackofficeStatisticsService(
            BackofficeStatisticsQueryRepository repository,
            @Qualifier("streakClock") Clock clock
    ) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public BackofficeStatisticsResponse getStatistics(LocalDate from, LocalDate to) {
        validateRange(from, to);

        return new BackofficeStatisticsResponse(
                repository.countUsersBefore(from.atStartOfDay()),
                repository.findDailyStatistics(from, to),
                repository.findDailySignups(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
        );
    }

    private void validateRange(LocalDate from, LocalDate to) {
        long rangeDays = ChronoUnit.DAYS.between(from, to) + 1;
        if (from.isAfter(to) || rangeDays > MAX_RANGE_DAYS || to.isAfter(LocalDate.now(clock))) {
            throw BaseException.of(
                    GlobalErrorCode.COMMON_INVALID_REQUEST,
                    "조회 기간은 오늘 이전의 1일 이상 365일 이하 범위여야 합니다."
            );
        }
    }
}
