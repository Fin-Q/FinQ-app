package com.swyp.FinQ.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 재설정 인증번호 발송 응답")
public record PasswordResetRequestResponse(
        @Schema(description = "인증번호 확인 시 전달할 인증 식별자", example = "d6b5f1a8-26a8-4d10-a63e-4ed33a54f846")
        String verificationId,
        @Schema(description = "인증번호 만료까지 남은 시간(초)", example = "300")
        long expiresIn,
        @Schema(description = "재전송 가능까지 남은 시간(초)", example = "60")
        long resendAvailableIn
) {
}
