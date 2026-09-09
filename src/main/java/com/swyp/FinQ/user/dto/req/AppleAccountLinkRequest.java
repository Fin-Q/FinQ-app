package com.swyp.FinQ.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "기존 회원의 Apple 계정 연동 요청")
public record AppleAccountLinkRequest(
        @Schema(description = "Apple Identity Token")
        @NotBlank(message = "Apple Identity Token은 필수입니다") String identityToken,
        @Schema(description = "Apple 일회용 Authorization Code")
        @NotBlank(message = "Apple Authorization Code는 필수입니다") String authorizationCode,
        @Schema(description = "Apple 인증 요청에 사용한 32자 원본 nonce. SHA-256 해시 전 값",
                example = "0123456789abcdef0123456789abcdef")
        @NotBlank(message = "Apple nonce는 필수입니다")
        @Size(min = 32, max = 32, message = "Apple nonce는 32자여야 합니다") String nonce
) {
}
