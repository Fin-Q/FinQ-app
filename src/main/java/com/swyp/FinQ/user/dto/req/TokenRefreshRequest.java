package com.swyp.FinQ.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record TokenRefreshRequest(
        @NotBlank(message = "Refresh Token은 필수입니다")
        @Schema(description = "FinQ Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken
) {
}
