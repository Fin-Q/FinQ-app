package com.swyp.FinQ.notification.dto.info;

import java.util.Map;

public record PushNotificationMessage(
        String title,
        String body,
        Map<String, String> data
) {

    public PushNotificationMessage {
        data = data == null ? Map.of() : Map.copyOf(data);
    }
}
