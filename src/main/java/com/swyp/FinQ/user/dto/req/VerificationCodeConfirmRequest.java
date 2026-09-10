package com.swyp.FinQ.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 재설정 인증번호 확인 요청")
public record VerificationCodeConfirmRequest(
        @NotBlank(message = "인증 식별자는 필수입니다")
        @Size(max = 64, message = "인증 식별자는 64자 이하여야 합니다")
        @Schema(description = "인증번호 발송 응답에서 받은 식별자", example = "d6b5f1a8-26a8-4d10-a63e-4ed33a54f846")
        String verificationId,
        @NotBlank(message = "인증번호는 필수입니다")
        @Pattern(regexp = "[0-9]{6}", message = "인증번호는 6자리 숫자여야 합니다")
        @Schema(description = "이메일로 받은 6자리 인증번호", example = "123456", pattern = "[0-9]{6}")
        String verificationCode
) {
}
