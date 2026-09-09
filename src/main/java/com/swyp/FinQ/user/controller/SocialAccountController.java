package com.swyp.FinQ.user.controller;

import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.user.dto.req.KakaoAccountLinkRequest;
import com.swyp.FinQ.user.dto.res.SocialAccountLinkResponse;
import com.swyp.FinQ.user.service.KakaoAccountLinkService;
import com.swyp.FinQ.user.success.UserSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me/social-accounts")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 계정 연동 API")
public class SocialAccountController {
    private final KakaoAccountLinkService kakaoAccountLinkService;

    @PostMapping("/kakao")
    @Operation(summary = "Kakao 계정 연동")
    public ResponseEntity<SuccessResponse<SocialAccountLinkResponse>> linkKakao(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody KakaoAccountLinkRequest request
    ) {
        return SuccessResponse.of(UserSuccessCode.KAKAO_ACCOUNT_LINKED,
                kakaoAccountLinkService.link(Long.valueOf(jwt.getSubject()), request));
    }
}
