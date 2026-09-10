package com.swyp.FinQ.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "약관 동의 항목")
public record AgreementRequest(
        @NotBlank(message = "약관 코드는 필수입니다")
        @Schema(description = "약관 코드", example = "TERMS_OF_SERVICE")
        String agreementCode,

        @NotBlank(message = "약관 버전은 필수입니다")
        @Schema(description = "동의한 약관 버전", example = "1.0")
        String version,

        @NotNull(message = "약관 동의 여부는 필수입니다")
        @Schema(description = "약관 동의 여부", example = "true")
        Boolean agreed
) {
}
