package com.swyp.FinQ.user.controller;

import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.user.dto.req.InterestSelectionRequest;
import com.swyp.FinQ.user.dto.req.NicknameUpdateRequest;
import com.swyp.FinQ.user.dto.req.ProfileImageUpdateRequest;
import com.swyp.FinQ.user.dto.res.NicknameUpdateResponse;
import com.swyp.FinQ.user.dto.res.ProfileImageUpdateResponse;
import com.swyp.FinQ.user.dto.res.MyPageResponse;
import com.swyp.FinQ.user.dto.res.OnboardingResponse;
import com.swyp.FinQ.user.service.OnboardingService;
import com.swyp.FinQ.user.service.UserProfileService;
import com.swyp.FinQ.user.success.UserSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 온보딩 및 프로필 API")
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

    private final OnboardingService onboardingService;
    private final UserProfileService userProfileService;

    @Operation(summary = "마이페이지 조회")
    @GetMapping
    public ResponseEntity<SuccessResponse<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return SuccessResponse.of(
                UserSuccessCode.MY_PAGE_RETRIEVED,
                userProfileService.getMyPage(Long.valueOf(jwt.getSubject()))
        );
    }

    @Operation(summary = "온보딩 상태 조회")
    @GetMapping("/onboarding")
    public ResponseEntity<SuccessResponse<OnboardingResponse>> getOnboarding(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return SuccessResponse.of(
                UserSuccessCode.ONBOARDING_RETRIEVED,
                onboardingService.getOnboarding(Long.valueOf(jwt.getSubject()))
        );
    }

    @Operation(summary = "관심 주제 최초 저장")
    @PostMapping("/interests")
    public ResponseEntity<SuccessResponse<OnboardingResponse>> selectInterests(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InterestSelectionRequest request
    ) {
        return SuccessResponse.of(
                UserSuccessCode.INTERESTS_CREATED,
                onboardingService.selectInterests(Long.valueOf(jwt.getSubject()), request)
        );
    }

    @Operation(summary = "관심 주제 수정")
    @PutMapping("/interests")
    public ResponseEntity<SuccessResponse<OnboardingResponse>> updateInterests(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InterestSelectionRequest request
    ) {
        return SuccessResponse.of(
                UserSuccessCode.INTERESTS_UPDATED,
                onboardingService.updateInterests(Long.valueOf(jwt.getSubject()), request)
        );
    }

    @Operation(summary = "온보딩 완료 처리")
    @PatchMapping("/onboarding/complete")
    public ResponseEntity<SuccessResponse<OnboardingResponse>> completeOnboarding(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return SuccessResponse.of(
                UserSuccessCode.ONBOARDING_COMPLETED,
                onboardingService.completeOnboarding(Long.valueOf(jwt.getSubject()))
        );
    }

    @Operation(summary = "닉네임 변경")
    @PatchMapping("/nickname")
    public ResponseEntity<SuccessResponse<NicknameUpdateResponse>> updateNickname(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody NicknameUpdateRequest request
    ) {
        return SuccessResponse.of(
                UserSuccessCode.NICKNAME_UPDATED,
                userProfileService.updateNickname(Long.valueOf(jwt.getSubject()), request)
        );
    }

    @Operation(summary = "프로필 이미지 변경")
    @PatchMapping("/profile-image")
    public ResponseEntity<SuccessResponse<ProfileImageUpdateResponse>> updateProfileImage(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProfileImageUpdateRequest request
    ) {
        return SuccessResponse.of(
                UserSuccessCode.PROFILE_IMAGE_UPDATED,
                userProfileService.updateProfileImage(Long.valueOf(jwt.getSubject()), request)
        );
    }
}
