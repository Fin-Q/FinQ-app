package com.swyp.FinQ.notification.dto.res;

import com.swyp.FinQ.notification.domain.PushPlatform;
import com.swyp.FinQ.notification.domain.PushToken;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Schema(description = "FCM 푸시 토큰 등록 또는 갱신 응답")
public record PushTokenRegistrationResponse(
        @Schema(description = "기기 식별자", example = "ios-device-01")
        String deviceId,
        @Schema(description = "기기 플랫폼. 현재 iOS만 지원", example = "IOS", allowableValues = "IOS")
        PushPlatform platform,
        @Schema(description = "등록 또는 갱신 일시, ISO 8601 KST 기준", example = "2026-09-10T10:30:00+09:00", format = "date-time")
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
