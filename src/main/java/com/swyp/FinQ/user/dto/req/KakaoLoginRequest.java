package com.swyp.FinQ.user.dto.req;

import com.swyp.FinQ.user.service.KakaoLoginCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Kakao 소셜 로그인 요청")
public record KakaoLoginRequest(
        @Schema(description = "Kakao SDK에서 발급받은 Access Token", example = "kakao_access_token")
        @NotBlank(message = "Kakao Access Token은 필수입니다")
        String kakaoAccessToken,

        @Schema(description = "신규 회원 닉네임. 기존 회원은 생략 가능", example = "Minter")
        @Size(max = 15, message = "닉네임은 15자 이하여야 합니다")
        @Pattern(regexp = ".*\\S.*", message = "닉네임은 공백일 수 없습니다")
        String nickname,

        @Schema(description = "신규 회원 약관 동의 목록. 기존 회원은 생략 가능")
        List<@NotNull(message = "약관 동의 항목은 null일 수 없습니다") @Valid AgreementRequest> agreements
) {

    public KakaoLoginCommand toCommand() {
        return new KakaoLoginCommand(kakaoAccessToken, nickname, agreements);
    }
}
