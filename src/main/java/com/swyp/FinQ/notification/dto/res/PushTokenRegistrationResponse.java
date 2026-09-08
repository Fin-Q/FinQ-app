package com.swyp.FinQ.notification.dto.res;

import com.swyp.FinQ.notification.domain.PushPlatform;
import com.swyp.FinQ.notification.domain.PushToken;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record PushTokenRegistrationResponse(
        String deviceId,
        PushPlatform platform,
        OffsetDateTime updatedAt
) {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    public static PushTokenRegistrationResponse from(PushToken pushToken) {
        return new PushTokenRegistrationResponse(
                pushToken.getDeviceId(),
                pushToken.getPlatform(),
                pushToken.getUpdatedAt().atZone(SERVICE_ZONE_ID).toOffsetDateTime()
        );
    }
}
