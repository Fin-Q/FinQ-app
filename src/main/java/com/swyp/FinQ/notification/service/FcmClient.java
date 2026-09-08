package com.swyp.FinQ.notification.service;

import com.swyp.FinQ.notification.dto.info.PushNotificationMessage;

import java.util.List;

public interface FcmClient {

    List<FcmSendOutcome> send(
            List<String> tokens,
            PushNotificationMessage message
    );
}
