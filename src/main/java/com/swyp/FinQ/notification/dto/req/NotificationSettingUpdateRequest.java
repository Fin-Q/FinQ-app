package com.swyp.FinQ.notification.dto.req;

import jakarta.validation.constraints.NotNull;

public record NotificationSettingUpdateRequest(
        @NotNull
        Boolean notificationEnabled
) {
}
