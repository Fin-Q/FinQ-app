package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.notification.domain.PushToken;
import com.swyp.FinQ.notification.dto.info.PushNotificationMessage;
import com.swyp.FinQ.notification.dto.info.PushNotificationSendResult;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
public class PushNotificationService {

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

        if (tokens.isEmpty()) {
            return PushNotificationSendResult.empty();
        }

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
}
