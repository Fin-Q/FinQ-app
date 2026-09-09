package com.swyp.FinQ.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.nio.charset.StandardCharsets;

public record PasswordResetConfirmRequest(
        @NotBlank(message = "비밀번호 재설정 토큰은 필수입니다")
        String passwordResetToken,
        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다")
        String newPassword
) {
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다")
    public boolean isPasswordWithinByteLimit() {
        return newPassword == null || newPassword.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
