package com.swyp.FinQ.streak.service;

import com.swyp.FinQ.streak.config.StreakConfig;
import com.swyp.FinQ.streak.repository.StreakDailyStatisticsRepository;
import com.swyp.FinQ.streak.repository.StreakStatisticsCounts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StreakStatisticsService {

    private final StreakDailyStatisticsRepository repository;
    private final Clock streakClock;

    @Transactional
    public void aggregate(LocalDate statisticsDate) {
        Objects.requireNonNull(statisticsDate, "statisticsDate must not be null");
        if (!statisticsDate.isBefore(LocalDate.now(streakClock.withZone(StreakConfig.STREAK_ZONE_ID)))) {
            throw new IllegalArgumentException("마감된 날짜만 스트릭 통계를 집계할 수 있습니다.");
        }

        LocalDateTime completionCutoff = statisticsDate.plusDays(1)
                .atStartOfDay(StreakConfig.STREAK_ZONE_ID)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
        StreakStatisticsCounts counts = repository.aggregate(
                statisticsDate, statisticsDate.minusDays(2), statisticsDate.minusDays(6), completionCutoff
        );
        repository.upsert(
                statisticsDate,
                counts.getLearnedUserCount(),
                counts.getStreak3DaysUserCount(),
                counts.getStreak7DaysUserCount(),
                LocalDateTime.ofInstant(streakClock.instant(), StreakConfig.STREAK_ZONE_ID)
        );
    }
}
