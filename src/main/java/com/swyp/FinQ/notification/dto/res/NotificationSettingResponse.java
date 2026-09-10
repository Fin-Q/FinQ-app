package com.swyp.FinQ.notification.dto.res;

import com.swyp.FinQ.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Schema(description = "알림 수신 설정 변경 응답")
public record NotificationSettingResponse(
        @Schema(description = "변경된 푸시 알림 수신 여부", example = "true")
        boolean notificationEnabled,
        @Schema(description = "변경 일시, ISO 8601 KST 기준", example = "2026-09-10T10:30:00+09:00", format = "date-time")
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
