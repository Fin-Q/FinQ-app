package com.swyp.FinQ.notification.dto.res;

import com.swyp.FinQ.user.domain.User;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record NotificationSettingResponse(
        boolean notificationEnabled,
        OffsetDateTime updatedAt
) {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    public static NotificationSettingResponse from(User user) {
        return new NotificationSettingResponse(
                user.isNotificationEnabled(),
                user.getUpdatedAt().atZone(SERVICE_ZONE_ID).toOffsetDateTime()
        );
    }
}
