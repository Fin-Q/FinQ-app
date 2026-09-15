package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.notification.domain.PushToken;
import com.swyp.FinQ.notification.dto.info.PushNotificationMessage;
import com.swyp.FinQ.notification.dto.info.PushNotificationSendResult;
import com.swyp.FinQ.notification.repository.PushTokenRepository;
import com.swyp.FinQ.notification.exception.PushNotificationDeliveryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 499, 500, 501, 1001})
    void sendsEnabledUsersInBatchesOfAtMost500(int tokenCount) {
        List<PushToken> tokens = tokens(tokenCount);
        stubCursorQuery(tokens);
        PushNotificationMessage message = message();
        if (tokenCount > 0) {
            when(fcmClient.send(anyList(), eq(message))).thenAnswer(invocation -> {
                List<String> batch = invocation.getArgument(0);
                assertThat(batch).hasSizeLessThanOrEqualTo(500);
                return batch.stream().map(token -> new FcmSendOutcome(token, FcmSendStatus.SUCCESS)).toList();
            });
        }

        PushNotificationSendResult result = service().sendToEnabledUsers(message);

        assertThat(result).isEqualTo(new PushNotificationSendResult(tokenCount, tokenCount, 0, 0));
        verify(fcmClient, times((tokenCount + 499) / 500)).send(anyList(), eq(message));
        verifyNoInteractions(pushTokenCleanupService);
    }

    @Test
    void continuesAfterRequestLevelFailureWithoutDeletingTokens() {
        stubCursorQuery(tokens(501));
        PushNotificationMessage message = message();
        when(fcmClient.send(anyList(), eq(message)))
                .thenThrow(new PushNotificationDeliveryException("request failed", new RuntimeException()))
                .thenReturn(List.of(new FcmSendOutcome("token-501", FcmSendStatus.SUCCESS)));

        assertThat(service().sendToEnabledUsers(message))
                .isEqualTo(new PushNotificationSendResult(501, 1, 500, 0));
        verify(fcmClient, times(2)).send(anyList(), eq(message));
        verifyNoInteractions(pushTokenCleanupService);
    }

    @Test
    void aggregatesInvalidAndTemporaryFailuresAcrossBatches() {
        stubCursorQuery(tokens(501));
        PushNotificationMessage message = message();
        when(fcmClient.send(anyList(), eq(message))).thenAnswer(invocation -> {
            List<String> batch = invocation.getArgument(0);
            return batch.stream().map(token -> new FcmSendOutcome(token,
                    token.equals("token-500") ? FcmSendStatus.INVALID_TOKEN
                            : token.equals("token-501") ? FcmSendStatus.FAILED : FcmSendStatus.SUCCESS)).toList();
        });
        when(pushTokenCleanupService.removeInvalidToken("token-500")).thenReturn(true);

        assertThat(service().sendToEnabledUsers(message))
                .isEqualTo(new PushNotificationSendResult(501, 499, 2, 1));
        verify(pushTokenCleanupService).removeInvalidToken("token-500");
        verify(pushTokenCleanupService, never()).removeInvalidToken("token-501");
    }

    @Test
    void alsoSplitsSingleUserSendingAt500Tokens() {
        when(pushTokenRepository.findAllByUser_IdAndUser_NotificationEnabledTrueAndActiveTrue(1L)).thenReturn(tokens(501));
        PushNotificationMessage message = message();
        when(fcmClient.send(anyList(), eq(message))).thenAnswer(invocation -> {
            List<String> batch = invocation.getArgument(0);
            assertThat(batch).hasSizeLessThanOrEqualTo(500);
            return batch.stream().map(token -> new FcmSendOutcome(token, FcmSendStatus.SUCCESS)).toList();
        });

        assertThat(service().sendToUser(1L, message)).isEqualTo(new PushNotificationSendResult(501, 501, 0, 0));
        verify(fcmClient, times(2)).send(anyList(), eq(message));
    }

    private List<PushToken> tokens(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(index -> PushToken.builder().id((long) index).fcmToken("token-" + index).build())
                .toList();
    }

    private void stubCursorQuery(List<PushToken> tokens) {
        when(pushTokenRepository.findByUser_NotificationEnabledTrueAndActiveTrueAndIdGreaterThanOrderByIdAsc(
                anyLong(), any(Pageable.class))).thenAnswer(invocation -> {
            long cursor = invocation.getArgument(0);
            Pageable pageable = invocation.getArgument(1);
            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(500);
            return tokens.stream().filter(token -> token.getId() > cursor)
                    .limit(pageable.getPageSize()).toList();
        });
    }

    private PushNotificationService service() {
        return new PushNotificationService(pushTokenRepository, fcmClient, pushTokenCleanupService);
    }

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
        when(pushTokenRepository.findAllByUser_IdAndUser_NotificationEnabledTrueAndActiveTrue(1L))
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
        when(pushTokenRepository.findAllByUser_IdAndUser_NotificationEnabledTrueAndActiveTrue(1L))
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
