package com.swyp.FinQ.streak.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StreakStatisticsSchedulerTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 15);

    @Mock
    private StreakStatisticsService service;

    private StreakStatisticsScheduler scheduler() {
        return new StreakStatisticsScheduler(service,
                Clock.fixed(Instant.parse("2026-09-15T15:10:00Z"), ZoneId.of("UTC")));
    }

    @Test
    void aggregatesPreviousKstDateWithUtcClock() {
        scheduler().aggregatePreviousDay();
        verify(service).aggregate(DATE);
    }

    @Test
    void schedulesAtTenMinutesAfterKstMidnightEachDay() throws Exception {
        Scheduled annotation = StreakStatisticsScheduler.class.getMethod("aggregatePreviousDay")
                .getAnnotation(Scheduled.class);
        assertThat(annotation.zone()).isEqualTo("Asia/Seoul");
        CronExpression cron = CronExpression.parse(annotation.cron());
        ZonedDateTime before = Instant.parse("2026-09-15T15:09:59Z")
                .atZone(ZoneId.of(annotation.zone()));
        ZonedDateTime first = cron.next(before);
        assertThat(first).isEqualTo(ZonedDateTime.parse("2026-09-16T00:10:00+09:00[Asia/Seoul]"));
        assertThat(cron.next(first)).isEqualTo(first.plusDays(1));
    }

    @Test
    void releasesGuardAfterFailureAllowingAnotherExecution() {
        doThrow(new IllegalStateException("database unavailable")).doNothing().when(service).aggregate(DATE);
        StreakStatisticsScheduler scheduler = scheduler();
        scheduler.aggregatePreviousDay();
        scheduler.aggregatePreviousDay();
        verify(service, times(2)).aggregate(DATE);
    }

    @Test
    void skipsOverlappingExecutionInSameInstance() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            entered.countDown();
            assertThat(release.await(5, TimeUnit.SECONDS)).isTrue();
            return null;
        }).when(service).aggregate(any());
        StreakStatisticsScheduler scheduler = scheduler();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var first = executor.submit(scheduler::aggregatePreviousDay);
            try {
                assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
                scheduler.aggregatePreviousDay();
            } finally {
                release.countDown();
            }
            first.get(5, TimeUnit.SECONDS);
        }
        verify(service).aggregate(DATE);
    }
}
