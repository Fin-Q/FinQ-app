package com.swyp.FinQ.notification.dto.req;

import com.swyp.FinQ.notification.domain.PushPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PushTokenRegistrationRequest(
        @NotBlank
        @Size(max = 2048)
        String fcmToken,

        @NotNull
        PushPlatform platform
) {
}
