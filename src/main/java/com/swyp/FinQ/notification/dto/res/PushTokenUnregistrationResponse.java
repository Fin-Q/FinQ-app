package com.swyp.FinQ.notification.dto.res;

import java.time.OffsetDateTime;

public record PushTokenUnregistrationResponse(
        String deviceId,
        OffsetDateTime unregisteredAt
) {
}
