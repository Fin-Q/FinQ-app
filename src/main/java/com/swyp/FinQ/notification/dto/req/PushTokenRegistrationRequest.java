package com.swyp.FinQ.notification.dto.req;

import com.swyp.FinQ.notification.domain.PushPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "FCM 푸시 토큰 등록 또는 갱신 요청")
public record PushTokenRegistrationRequest(
        @NotBlank
        @Size(max = 2048)
        @Schema(description = "Firebase SDK에서 발급받은 FCM 등록 토큰", example = "fcm-registration-token")
        String fcmToken,

        @NotNull
        @Schema(description = "기기 플랫폼. 현재 iOS만 지원", example = "IOS", allowableValues = "IOS")
        PushPlatform platform
) {
}
