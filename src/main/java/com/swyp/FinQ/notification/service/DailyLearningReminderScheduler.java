package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.notification.dto.info.PushNotificationMessage;
import com.swyp.FinQ.notification.dto.info.PushNotificationSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class DailyLearningReminderScheduler {

    private final PushNotificationService pushNotificationService;
    private final AtomicBoolean running = new AtomicBoolean();

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void sendDailyReminder() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Daily learning reminder skipped: execution already running in this instance");
            return;
        }

        try {
            PushNotificationMessage message = new PushNotificationMessage(
                    "오늘의 금융 질문, 궁금하지 않나요?",
                    "3분이면 하나씩 알아갈 수 있어요.",
                    Map.of());
            PushNotificationSendResult result = pushNotificationService.sendToEnabledUsers(message);
            log.info("Daily learning reminder completed: requestedCount={}, successCount={}, failureCount={}, invalidTokenRemovedCount={}",
                    result.requestedCount(), result.successCount(), result.failureCount(), result.invalidTokenRemovedCount());
        } catch (RuntimeException exception) {
            log.error("Daily learning reminder execution failed: causeType={}", exception.getClass().getSimpleName());
        } finally {
            running.set(false);
        }
    }
}
