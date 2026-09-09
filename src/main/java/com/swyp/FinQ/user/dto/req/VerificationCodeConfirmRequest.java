package com.swyp.FinQ.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerificationCodeConfirmRequest(
        @NotBlank(message = "인증 식별자는 필수입니다")
        @Size(max = 64, message = "인증 식별자는 64자 이하여야 합니다")
        String verificationId,
        @NotBlank(message = "인증번호는 필수입니다")
        @Pattern(regexp = "[0-9]{6}", message = "인증번호는 6자리 숫자여야 합니다")
        String verificationCode
) {
}
