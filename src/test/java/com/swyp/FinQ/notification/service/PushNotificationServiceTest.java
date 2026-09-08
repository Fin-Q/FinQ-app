package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.notification.domain.PushToken;
import com.swyp.FinQ.notification.dto.info.PushNotificationMessage;
import com.swyp.FinQ.notification.dto.info.PushNotificationSendResult;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock
    private PushTokenRepository pushTokenRepository;

    @Mock
    private FcmClient fcmClient;

    @Mock
    private PushTokenCleanupService pushTokenCleanupService;

    @Mock
    private PushToken firstToken;

    @Mock
    private PushToken secondToken;

    @Mock
    private PushToken thirdToken;

    @Test
    @DisplayName("알림이 활성화된 사용자의 토큰에 발송하고 영구 무효 토큰만 정리한다")
    void sendsAndRemovesOnlyInvalidTokens() {
        PushNotificationMessage message = message();
        when(pushTokenRepository.findAllByUser_IdAndUser_NotificationEnabledTrue(1L))
                .thenReturn(List.of(firstToken, secondToken, thirdToken));
        when(firstToken.getFcmToken()).thenReturn("success-token");
        when(secondToken.getFcmToken()).thenReturn("invalid-token");
        when(thirdToken.getFcmToken()).thenReturn("temporary-failure-token");
        when(fcmClient.send(
                List.of("success-token", "invalid-token", "temporary-failure-token"),
                message
        )).thenReturn(List.of(
                new FcmSendOutcome("success-token", FcmSendStatus.SUCCESS),
                new FcmSendOutcome("invalid-token", FcmSendStatus.INVALID_TOKEN),
                new FcmSendOutcome("temporary-failure-token", FcmSendStatus.FAILED)
        ));
        when(pushTokenCleanupService.removeInvalidToken("invalid-token")).thenReturn(true);

        PushNotificationSendResult result = new PushNotificationService(
                pushTokenRepository,
                fcmClient,
                pushTokenCleanupService
        ).sendToUser(1L, message);

        assertThat(result).isEqualTo(new PushNotificationSendResult(3, 1, 2, 1));
        verify(pushTokenCleanupService).removeInvalidToken("invalid-token");
        verify(pushTokenCleanupService, never())
                .removeInvalidToken("temporary-failure-token");
    }

    @Test
    @DisplayName("발송 가능한 토큰이 없으면 FCM을 호출하지 않는다")
    void skipsSendingWithoutTokens() {
        when(pushTokenRepository.findAllByUser_IdAndUser_NotificationEnabledTrue(1L))
                .thenReturn(List.of());

        PushNotificationSendResult result = new PushNotificationService(
                pushTokenRepository,
                fcmClient,
                pushTokenCleanupService
        ).sendToUser(1L, message());

        assertThat(result).isEqualTo(PushNotificationSendResult.empty());
        verifyNoInteractions(fcmClient, pushTokenCleanupService);
    }

    private PushNotificationMessage message() {
        return new PushNotificationMessage(
                "학습 알림",
                "오늘의 학습을 시작해 보세요.",
                Map.of("type", "LEARNING_REMINDER")
        );
    }
}
