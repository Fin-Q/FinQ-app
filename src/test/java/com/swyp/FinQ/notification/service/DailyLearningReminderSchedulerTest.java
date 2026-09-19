package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.notification.dto.info.PushNotificationMessage;
import com.swyp.FinQ.notification.dto.info.PushNotificationSendResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyLearningReminderSchedulerTest {

    @Mock
    private PushNotificationService service;

    private DailyLearningReminderScheduler scheduler() {
        return new DailyLearningReminderScheduler(service);
    }

    @Test
    void sendsFixedCopyWithoutRoutingData() {
        when(service.sendToEnabledUsers(any())).thenReturn(new PushNotificationSendResult(501, 499, 2, 1));
        scheduler().sendDailyReminder();

        ArgumentCaptor<PushNotificationMessage> message = ArgumentCaptor.forClass(PushNotificationMessage.class);
        verify(service).sendToEnabledUsers(message.capture());
        assertThat(message.getValue().title()).isEqualTo("오늘의 금융 질문, 궁금하지 않나요?");
        assertThat(message.getValue().body()).isEqualTo("3분이면 하나씩 알아갈 수 있어요.");
        assertThat(message.getValue().data()).isEmpty();
    }

    @Test
    void schedulesAt10PmSeoulEachDayIndependentOfServerTimezone() throws Exception {
        Scheduled annotation = DailyLearningReminderScheduler.class.getMethod("sendDailyReminder")
                .getAnnotation(Scheduled.class);
        assertThat(annotation.zone()).isEqualTo("Asia/Seoul");
        CronExpression cron = CronExpression.parse(annotation.cron());
        ZoneId seoul = ZoneId.of(annotation.zone());
        ZonedDateTime before = ZonedDateTime.parse("2026-09-13T00:59:59Z").withZoneSameInstant(seoul);
        ZonedDateTime first = cron.next(before);
        assertThat(first).isEqualTo(ZonedDateTime.parse("2026-09-13T22:00:00+09:00[Asia/Seoul]"));
        assertThat(cron.next(first)).isEqualTo(first.plusDays(1));
    }

    @Test
    void releasesExecutionGuardAfterFailure() {
        when(service.sendToEnabledUsers(any())).thenThrow(new IllegalStateException("database unavailable"))
                .thenReturn(PushNotificationSendResult.empty());
        DailyLearningReminderScheduler scheduler = scheduler();
        scheduler.sendDailyReminder();
        scheduler.sendDailyReminder();
        verify(service, times(2)).sendToEnabledUsers(any());
    }

    @Test
    void skipsOverlappingExecutionInSameInstance() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(service.sendToEnabledUsers(any())).thenAnswer(invocation -> {
            entered.countDown();
            assertThat(release.await(5, TimeUnit.SECONDS)).isTrue();
            return PushNotificationSendResult.empty();
        });
        DailyLearningReminderScheduler scheduler = scheduler();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var first = executor.submit(scheduler::sendDailyReminder);
            try {
                assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
                scheduler.sendDailyReminder();
            } finally {
                release.countDown();
            }
            first.get(5, TimeUnit.SECONDS);
        }
        verify(service).sendToEnabledUsers(any());
    }
}
