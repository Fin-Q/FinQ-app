package com.swyp.FinQ.notification.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "알림 수신 설정 변경 요청")
public record NotificationSettingUpdateRequest(
        @NotNull
        @Schema(description = "푸시 알림 수신 여부", example = "true")
        Boolean notificationEnabled
) {
}
