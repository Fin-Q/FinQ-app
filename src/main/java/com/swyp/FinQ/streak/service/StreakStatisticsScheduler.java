package com.swyp.FinQ.streak.service;

import com.swyp.FinQ.streak.config.StreakConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class StreakStatisticsScheduler {

    private final StreakStatisticsService service;
    private final Clock streakClock;
    private final AtomicBoolean running = new AtomicBoolean();

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void aggregatePreviousDay() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Streak statistics skipped: execution already running in this instance");
            return;
        }

        LocalDate statisticsDate = null;
        try {
            statisticsDate = LocalDate.now(streakClock.withZone(StreakConfig.STREAK_ZONE_ID)).minusDays(1);
            service.aggregate(statisticsDate);
            log.info("Streak statistics completed: statisticsDate={}", statisticsDate);
        } catch (RuntimeException exception) {
            log.error("Streak statistics failed: statisticsDate={}", statisticsDate, exception);
        } finally {
            running.set(false);
        }
    }
}
