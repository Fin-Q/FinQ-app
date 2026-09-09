package com.swyp.FinQ.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "기존 회원의 Kakao 계정 연동 요청")
public record KakaoAccountLinkRequest(
        @Schema(description = "연동할 Kakao 계정의 Access Token")
        @NotBlank(message = "Kakao Access Token은 필수입니다")
        String kakaoAccessToken
) {
}
