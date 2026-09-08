package com.swyp.FinQ.notification.controller;

import com.swyp.FinQ.global.security.token.JwtClaimNames;
import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.notification.dto.req.PushTokenRegistrationRequest;
import com.swyp.FinQ.notification.dto.res.PushTokenRegistrationResponse;
import com.swyp.FinQ.notification.service.PushTokenService;
import com.swyp.FinQ.notification.success.NotificationSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "푸시 토큰 및 알림 설정 API")
@Validated
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class NotificationController {

    private final PushTokenService pushTokenService;

    @Operation(summary = "FCM 푸시 토큰 등록 또는 갱신")
    @PostMapping("/push-tokens/{deviceId}")
    public ResponseEntity<SuccessResponse<PushTokenRegistrationResponse>> registerPushToken(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable @NotBlank @Size(max = 255) String deviceId,
            @Valid @RequestBody PushTokenRegistrationRequest request
    ) {
        return SuccessResponse.of(
                NotificationSuccessCode.PUSH_TOKEN_REGISTERED,
                pushTokenService.register(
                        Long.valueOf(jwt.getSubject()),
                        jwt.getClaimAsString(JwtClaimNames.SESSION_ID),
                        deviceId,
                        request
                )
        );
    }
}
