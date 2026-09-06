package com.swyp.FinQ.user.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SignUpRequest(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        @Size(max = 255, message = "이메일은 255자 이하여야 합니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다")
        String password,

        @NotBlank(message = "닉네임은 필수입니다")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
        String nickname,

        @NotEmpty(message = "필수 약관 동의 목록은 비어 있을 수 없습니다")
        List<@NotNull(message = "약관 동의 항목은 null일 수 없습니다") @Valid AgreementRequest> agreements
) {
}
