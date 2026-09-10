package com.swyp.FinQ.user.controller;

import com.swyp.FinQ.global.config.ApiDocumentation;
import com.swyp.FinQ.global.config.ApiOwner;
import com.swyp.FinQ.global.config.ApiTags;
import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.global.security.token.JwtClaimNames;
import com.swyp.FinQ.user.dto.req.AppleLoginRequest;
import com.swyp.FinQ.user.dto.req.LoginRequest;
import com.swyp.FinQ.user.dto.req.KakaoLoginRequest;
import com.swyp.FinQ.user.dto.req.PasswordResetEmailRequest;
import com.swyp.FinQ.user.dto.req.PasswordResetConfirmRequest;
import com.swyp.FinQ.user.service.PasswordResetService;
import com.swyp.FinQ.user.service.VerificationCodeConfirmService;
import com.swyp.FinQ.user.dto.req.VerificationCodeConfirmRequest;
import com.swyp.FinQ.user.dto.res.VerificationCodeConfirmResponse;
import com.swyp.FinQ.user.dto.req.SignUpRequest;
import com.swyp.FinQ.user.dto.req.TokenRefreshRequest;
import com.swyp.FinQ.user.dto.res.AgreementListResponse;
import com.swyp.FinQ.user.dto.res.AppleLoginResponse;
import com.swyp.FinQ.user.dto.res.LoginResponse;
import com.swyp.FinQ.user.dto.res.KakaoLoginResponse;
import com.swyp.FinQ.user.dto.res.PasswordResetRequestResponse;
import com.swyp.FinQ.user.dto.res.SignUpResponse;
import com.swyp.FinQ.user.dto.res.TokenRefreshResponse;
import com.swyp.FinQ.user.service.AgreementQueryService;
import com.swyp.FinQ.user.service.AppleLoginService;
import com.swyp.FinQ.user.service.LoginService;
import com.swyp.FinQ.user.service.KakaoLoginService;
import com.swyp.FinQ.user.service.LogoutService;
import com.swyp.FinQ.user.service.PasswordResetRequestService;
import com.swyp.FinQ.user.service.SignUpService;
import com.swyp.FinQ.user.service.TokenRefreshService;
import com.swyp.FinQ.user.success.AuthSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.USER, description = "회원 인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AgreementQueryService agreementQueryService;
    private final SignUpService signUpService;
    private final LoginService loginService;
    private final KakaoLoginService kakaoLoginService;
    private final AppleLoginService appleLoginService;
    private final TokenRefreshService tokenRefreshService;
    private final LogoutService logoutService;
    private final PasswordResetRequestService passwordResetRequestService;
    private final PasswordResetService passwordResetService;
    private final VerificationCodeConfirmService verificationCodeConfirmService;

    @Operation(summary = "현재 적용 중인 필수 약관 목록 조회")
    @ApiDocumentation(name = "현재 적용 중인 필수 약관 목록 조회", owner = ApiOwner.MINJI, secured = false)
    @GetMapping("/agreements")
    public ResponseEntity<SuccessResponse<AgreementListResponse>> getAgreements() {
        return SuccessResponse.of(
                AuthSuccessCode.AGREEMENTS_RETRIEVED,
                agreementQueryService.getCurrentAgreements()
        );
    }

    @Operation(summary = "회원가입")
    @ApiDocumentation(id = "USER-001", name = "회원가입", owner = ApiOwner.MINJI, secured = false)
    @PostMapping("/sign-up")
    public ResponseEntity<SuccessResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        return SuccessResponse.of(AuthSuccessCode.SIGN_UP, signUpService.signUp(request));
    }

    @Operation(summary = "일반 로그인")
    @ApiDocumentation(id = "USER-002", name = "일반 로그인", owner = ApiOwner.MINJI, secured = false)
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return SuccessResponse.of(AuthSuccessCode.LOGIN, loginService.login(request));
    }

    @Operation(
            summary = "Kakao 소셜 로그인",
            description = "Kakao Access Token을 검증하고 기존 회원 로그인 또는 신규 회원 가입을 처리합니다. "
                    + "닉네임과 약관 동의는 신규 회원에게만 필요합니다."
    )
    @ApiDocumentation(id = "USER-004", name = "Kakao 소셜 로그인", owner = ApiOwner.MINJI, secured = false)
    @PostMapping("/social/kakao")
    public ResponseEntity<SuccessResponse<KakaoLoginResponse>> loginWithKakao(
            @Valid @RequestBody KakaoLoginRequest request
    ) {
        return SuccessResponse.of(
                AuthSuccessCode.KAKAO_LOGIN,
                KakaoLoginResponse.from(kakaoLoginService.login(request.toCommand()))
        );
    }

    @Operation(
            summary = "Apple 소셜 로그인",
            description = "Apple Identity Token, Authorization Code와 원본 nonce를 검증하고 "
                    + "기존 회원 로그인 또는 신규 회원 가입을 처리합니다. "
                    + "닉네임과 약관 동의는 신규 회원에게만 필요합니다."
    )
    @ApiDocumentation(id = "USER-003", name = "Apple 소셜 로그인", owner = ApiOwner.MINJI, secured = false)
    @PostMapping("/social/apple")
    public ResponseEntity<SuccessResponse<AppleLoginResponse>> loginWithApple(
            @Valid @RequestBody AppleLoginRequest request
    ) {
        return SuccessResponse.of(
                AuthSuccessCode.APPLE_LOGIN,
                AppleLoginResponse.from(appleLoginService.login(request.toCommand()))
        );
    }

    @Operation(summary = "토큰 재발급")
    @ApiDocumentation(id = "USER-005", name = "Access Token 재발급", owner = ApiOwner.MINJI, secured = false)
    @PostMapping("/token/refresh")
    public ResponseEntity<SuccessResponse<TokenRefreshResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        return SuccessResponse.of(AuthSuccessCode.TOKEN_REFRESH, tokenRefreshService.refresh(request));
    }

    @Operation(summary = "로그아웃")
    @ApiDocumentation(id = "USER-006", name = "로그아웃", owner = ApiOwner.MINJI)
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(@AuthenticationPrincipal Jwt jwt) {
        logoutService.logout(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString(JwtClaimNames.SESSION_ID)
        );
        return SuccessResponse.of(AuthSuccessCode.LOGOUT, null);
    }

    @Operation(summary = "비밀번호 재설정 인증번호 전송")
    @ApiDocumentation(id = "USER-007", name = "인증번호 전송", owner = ApiOwner.MINJI, secured = false)
    @PostMapping("/password-reset/verifications")
    public ResponseEntity<SuccessResponse<PasswordResetRequestResponse>> requestPasswordReset(
            @Valid @RequestBody PasswordResetEmailRequest request
    ) {
        return SuccessResponse.of(
                AuthSuccessCode.PASSWORD_RESET_CODE_SENT,
                passwordResetRequestService.request(request.loginId())
        );
    }

    @Operation(summary = "비밀번호 재설정")
    @ApiDocumentation(id = "USER-009", name = "비밀번호 재설정", owner = ApiOwner.MINJI, secured = false)
    @PostMapping("/password-reset")
    public ResponseEntity<SuccessResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        passwordResetService.reset(request);
        return SuccessResponse.of(AuthSuccessCode.PASSWORD_RESET, null);
    }

    @Operation(summary = "비밀번호 재설정 인증번호 확인")
    @ApiDocumentation(id = "USER-008", name = "인증번호 확인", owner = ApiOwner.MINJI, secured = false)
    @PostMapping("/password-reset/verifications/confirm")
    public ResponseEntity<SuccessResponse<VerificationCodeConfirmResponse>> confirmVerificationCode(
            @Valid @RequestBody VerificationCodeConfirmRequest request
    ) {
        return SuccessResponse.of(AuthSuccessCode.VERIFICATION_CODE_CONFIRMED,
                verificationCodeConfirmService.confirm(request));
    }
}
