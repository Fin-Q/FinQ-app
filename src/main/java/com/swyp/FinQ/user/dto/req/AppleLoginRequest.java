package com.swyp.FinQ.user.dto.req;

import com.swyp.FinQ.user.service.AppleLoginCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Apple 소셜 로그인 요청")
public record AppleLoginRequest(
        @Schema(description = "Apple에서 발급받은 Identity Token")
        @NotBlank(message = "Apple Identity Token은 필수입니다")
        String identityToken,

        @Schema(description = "Apple에서 발급받은 일회용 Authorization Code")
        @NotBlank(message = "Apple Authorization Code는 필수입니다")
        String authorizationCode,

        @Schema(description = "Apple 인증 요청에 사용한 원본 nonce", example = "one-time-raw-nonce")
        @NotBlank(message = "Apple nonce는 필수입니다")
        String nonce,

        @Schema(description = "신규 회원 닉네임. 기존 회원은 생략 가능", example = "Minter")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
        @Pattern(regexp = ".*\\S.*", message = "닉네임은 공백일 수 없습니다")
        String nickname,

        @Schema(description = "신규 회원 약관 동의 목록. 기존 회원은 생략 가능")
        List<@NotNull(message = "약관 동의 항목은 null일 수 없습니다") @Valid AgreementRequest> agreements
) {

    public AppleLoginCommand toCommand() {
        return new AppleLoginCommand(
                identityToken,
                authorizationCode,
                nonce,
                nickname,
                agreements
        );
    }
}
