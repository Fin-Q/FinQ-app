package com.swyp.FinQ.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AgreementRequest(
        @NotBlank(message = "약관 코드는 필수입니다")
        String agreementCode,

        @NotBlank(message = "약관 버전은 필수입니다")
        String version,

        @NotNull(message = "약관 동의 여부는 필수입니다")
        Boolean agreed
) {
}
