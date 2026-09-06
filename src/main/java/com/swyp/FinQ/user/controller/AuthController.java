package com.swyp.FinQ.user.controller;

import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.global.security.token.JwtClaimNames;
import com.swyp.FinQ.user.dto.req.LoginRequest;
import com.swyp.FinQ.user.dto.req.SignUpRequest;
import com.swyp.FinQ.user.dto.req.TokenRefreshRequest;
import com.swyp.FinQ.user.dto.res.AgreementListResponse;
import com.swyp.FinQ.user.dto.res.LoginResponse;
import com.swyp.FinQ.user.dto.res.SignUpResponse;
import com.swyp.FinQ.user.dto.res.TokenRefreshResponse;
import com.swyp.FinQ.user.service.AgreementQueryService;
import com.swyp.FinQ.user.service.LoginService;
import com.swyp.FinQ.user.service.LogoutService;
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

@Tag(name = "Auth", description = "회원 인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AgreementQueryService agreementQueryService;
    private final SignUpService signUpService;
    private final LoginService loginService;
    private final TokenRefreshService tokenRefreshService;
    private final LogoutService logoutService;

    @Operation(summary = "현재 적용 중인 필수 약관 목록 조회")
    @GetMapping("/agreements")
    public ResponseEntity<SuccessResponse<AgreementListResponse>> getAgreements() {
        return SuccessResponse.of(
                AuthSuccessCode.AGREEMENTS_RETRIEVED,
                agreementQueryService.getCurrentAgreements()
        );
    }

    @Operation(summary = "회원가입")
    @PostMapping("/sign-up")
    public ResponseEntity<SuccessResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        return SuccessResponse.of(AuthSuccessCode.SIGN_UP, signUpService.signUp(request));
    }

    @Operation(summary = "일반 로그인")
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return SuccessResponse.of(AuthSuccessCode.LOGIN, loginService.login(request));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/token/refresh")
    public ResponseEntity<SuccessResponse<TokenRefreshResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        return SuccessResponse.of(AuthSuccessCode.TOKEN_REFRESH, tokenRefreshService.refresh(request));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(@AuthenticationPrincipal Jwt jwt) {
        logoutService.logout(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString(JwtClaimNames.SESSION_ID)
        );
        return SuccessResponse.of(AuthSuccessCode.LOGOUT, null);
    }
}
