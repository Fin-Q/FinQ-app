package com.swyp.FinQ.notification.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "FCM 푸시 토큰 등록 해제 응답")
public record PushTokenUnregistrationResponse(
        @Schema(description = "등록 해제된 기기 식별자", example = "ios-device-01")
        String deviceId,
        @Schema(description = "등록 해제 일시, ISO 8601 KST 기준", example = "2026-09-10T10:30:00+09:00", format = "date-time")
        OffsetDateTime unregisteredAt
) {
}
