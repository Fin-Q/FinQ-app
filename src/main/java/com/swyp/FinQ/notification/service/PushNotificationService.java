package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.notification.domain.PushToken;
import com.swyp.FinQ.notification.dto.info.PushNotificationMessage;
import com.swyp.FinQ.notification.dto.info.PushNotificationSendResult;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.notification.exception.PushNotificationDeliveryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
public class PushNotificationService {

    private static final int BATCH_SIZE = 500;

    private final PushTokenRepository pushTokenRepository;
    private final FcmClient fcmClient;
    private final PushTokenCleanupService pushTokenCleanupService;

    public PushNotificationSendResult sendToUser(
            Long userId,
            PushNotificationMessage message
    ) {
        List<String> tokens = pushTokenRepository
                .findAllByUser_IdAndUser_NotificationEnabledTrue(userId)
                .stream()
                .map(PushToken::getFcmToken)
                .toList();

        PushNotificationSendResult result = PushNotificationSendResult.empty();
        for (int start = 0; start < tokens.size(); start += BATCH_SIZE) {
            result = add(result, sendBatch(tokens.subList(start, Math.min(start + BATCH_SIZE, tokens.size())), message));
        }
        return result;
    }

    public PushNotificationSendResult sendToEnabledUsers(PushNotificationMessage message) {
        long afterId = 0L;
        PushNotificationSendResult result = PushNotificationSendResult.empty();
        while (true) {
            List<PushToken> batch = pushTokenRepository
                    .findByUser_NotificationEnabledTrueAndIdGreaterThanOrderByIdAsc(
                            afterId, PageRequest.of(0, BATCH_SIZE));
            if (batch.isEmpty()) {
                return result;
            }

            afterId = batch.getLast().getId();
            List<String> tokens = batch.stream().map(PushToken::getFcmToken).toList();
            PushNotificationSendResult batchResult;
            try {
                batchResult = sendBatch(tokens, message);
            } catch (PushNotificationDeliveryException exception) {

                log.warn("FCM batch request failed: requestedCount={}, causeType={}",
                        tokens.size(), exception.getClass().getSimpleName());
                batchResult = new PushNotificationSendResult(tokens.size(), 0, tokens.size(), 0);
            }
            result = add(result, batchResult);
        }
    }

    private PushNotificationSendResult sendBatch(List<String> tokens, PushNotificationMessage message) {

        List<FcmSendOutcome> outcomes = fcmClient.send(tokens, message);
        int successCount = 0;
        int invalidTokenRemovedCount = 0;

        for (FcmSendOutcome outcome : outcomes) {
            if (outcome.status() == FcmSendStatus.SUCCESS) {
                successCount++;
            } else if (outcome.status() == FcmSendStatus.INVALID_TOKEN
                    && pushTokenCleanupService.removeInvalidToken(outcome.token())) {
                invalidTokenRemovedCount++;
            }
        }

        return new PushNotificationSendResult(
                tokens.size(),
                successCount,
                tokens.size() - successCount,
                invalidTokenRemovedCount
        );
    }

    private PushNotificationSendResult add(PushNotificationSendResult left, PushNotificationSendResult right) {
        return new PushNotificationSendResult(
                left.requestedCount() + right.requestedCount(),
                left.successCount() + right.successCount(),
                left.failureCount() + right.failureCount(),
                left.invalidTokenRemovedCount() + right.invalidTokenRemovedCount());
    }
}
