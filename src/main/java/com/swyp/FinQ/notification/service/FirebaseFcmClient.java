package com.swyp.FinQ.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.swyp.FinQ.notification.dto.info.PushNotificationMessage;
import com.swyp.FinQ.notification.exception.PushNotificationDeliveryException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
public class FirebaseFcmClient implements FcmClient {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public List<FcmSendOutcome> send(
            List<String> tokens,
            PushNotificationMessage message
    ) {
        if (tokens.isEmpty()) {
            return List.of();
        }

        MulticastMessage multicastMessage = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(message.title())
                        .setBody(message.body())
                        .build())
                .putAllData(message.data())
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse batchResponse = firebaseMessaging.sendEachForMulticast(multicastMessage);
            return toOutcomes(tokens, batchResponse.getResponses());
        } catch (FirebaseMessagingException exception) {
            throw new PushNotificationDeliveryException(
                    "FCM multicast request failed",
                    exception
            );
        }
    }

    private List<FcmSendOutcome> toOutcomes(
            List<String> tokens,
            List<SendResponse> responses
    ) {
        if (tokens.size() != responses.size()) {
            throw new IllegalStateException("FCM response count does not match token count");
        }

        List<FcmSendOutcome> outcomes = new ArrayList<>(tokens.size());
        for (int index = 0; index < tokens.size(); index++) {
            SendResponse response = responses.get(index);
            FcmSendStatus status = response.isSuccessful()
                    ? FcmSendStatus.SUCCESS
                    : classify(response.getException());
            outcomes.add(new FcmSendOutcome(tokens.get(index), status));
        }
        return List.copyOf(outcomes);
    }

    FcmSendStatus classify(FirebaseMessagingException exception) {
        if (exception == null) {
            return FcmSendStatus.FAILED;
        }

        MessagingErrorCode errorCode = exception.getMessagingErrorCode();
        if (errorCode == MessagingErrorCode.UNREGISTERED
                || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
            return FcmSendStatus.INVALID_TOKEN;
        }
        return FcmSendStatus.FAILED;
    }
}
