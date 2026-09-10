package com.swyp.FinQ.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 재설정 인증번호 확인 응답")
public record VerificationCodeConfirmResponse(
        @Schema(description = "새 비밀번호 설정에 사용할 일회용 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String passwordResetToken,
        @Schema(description = "비밀번호 재설정 토큰 만료까지 남은 시간(초)", example = "600")
        long expiresIn
) {
}
