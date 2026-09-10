package com.swyp.FinQ.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 재설정 인증번호 발송 요청")
public record PasswordResetEmailRequest(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        @Size(max = 255, message = "이메일은 255자 이하여야 합니다")
        @Schema(description = "비밀번호를 재설정할 가입 이메일", example = "user@example.com", format = "email")
        String loginId
) {
}
