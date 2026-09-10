package com.swyp.FinQ.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "일반 로그인 요청")
public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        @Size(max = 255, message = "이메일은 255자 이하여야 합니다")
        @Schema(description = "가입 이메일", example = "user@example.com", format = "email")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(max = 72, message = "비밀번호는 72자 이하여야 합니다")
        @Schema(description = "계정 비밀번호", example = "Finq1234!")
        String password
) {
}
