package com.swyp.FinQ.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.swyp.FinQ.notification.dto.info.PushNotificationMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebaseFcmClientTest {

    private final FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
    private final FirebaseFcmClient fcmClient = new FirebaseFcmClient(firebaseMessaging);

    @Test
    @DisplayName("FCM 응답 순서에 맞춰 토큰별 발송 결과를 반환한다")
    void mapsResponsesToTokensInOrder() throws FirebaseMessagingException {
        BatchResponse batchResponse = mock(BatchResponse.class);
        SendResponse successfulResponse = mock(SendResponse.class);
        SendResponse invalidTokenResponse = mock(SendResponse.class);
        FirebaseMessagingException invalidTokenException = exceptionOf(
                MessagingErrorCode.UNREGISTERED
        );
        when(successfulResponse.isSuccessful()).thenReturn(true);
        when(invalidTokenResponse.isSuccessful()).thenReturn(false);
        when(invalidTokenResponse.getException())
                .thenReturn(invalidTokenException);
        when(batchResponse.getResponses())
                .thenReturn(List.of(successfulResponse, invalidTokenResponse));
        when(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class)))
                .thenReturn(batchResponse);

        List<FcmSendOutcome> outcomes = fcmClient.send(
                List.of("first-token", "second-token"),
                new PushNotificationMessage("제목", "내용", Map.of("type", "TEST"))
        );

        assertThat(outcomes).containsExactly(
                new FcmSendOutcome("first-token", FcmSendStatus.SUCCESS),
                new FcmSendOutcome("second-token", FcmSendStatus.INVALID_TOKEN)
        );
    }

    @Test
    @DisplayName("FCM의 영구 무효 토큰 오류를 정리 대상으로 분류한다")
    void classifiesPermanentInvalidTokenErrors() {
        assertThat(fcmClient.classify(exceptionOf(MessagingErrorCode.UNREGISTERED)))
                .isEqualTo(FcmSendStatus.INVALID_TOKEN);
        assertThat(fcmClient.classify(exceptionOf(MessagingErrorCode.INVALID_ARGUMENT)))
                .isEqualTo(FcmSendStatus.INVALID_TOKEN);
    }

    @Test
    @DisplayName("FCM의 일시 오류는 토큰 정리 대상으로 분류하지 않는다")
    void doesNotClassifyTransientErrorsAsInvalidToken() {
        assertThat(fcmClient.classify(exceptionOf(MessagingErrorCode.UNAVAILABLE)))
                .isEqualTo(FcmSendStatus.FAILED);
        assertThat(fcmClient.classify(exceptionOf(MessagingErrorCode.INTERNAL)))
                .isEqualTo(FcmSendStatus.FAILED);
    }

    private FirebaseMessagingException exceptionOf(MessagingErrorCode errorCode) {
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessagingErrorCode()).thenReturn(errorCode);
        return exception;
    }
}
