package com.swyp.FinQ.home.controller;

import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.global.config.ApiDocumentation;
import com.swyp.FinQ.home.dto.res.HomeResponse;
import com.swyp.FinQ.home.service.HomeQueryService;
import com.swyp.FinQ.home.success.HomeSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 화면 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeQueryService homeQueryService;

    @Operation(
            operationId = "HOME-001",
            summary = "[HOME-001] 홈 화면 조회",
            description = "사용자 정보, 캐릭터/레벨, XP, 스트릭, 추천 질문 카드를 조회합니다.",
            extensions = @Extension(properties = @ExtensionProperty(name = "x-owner", value = "yezanee"))
    )
    @GetMapping("/home")
    public ResponseEntity<SuccessResponse<HomeResponse>> getHome(
            @AuthenticationPrincipal Jwt jwt
    ) {
        HomeResponse response = homeQueryService.getHome(Long.valueOf(jwt.getSubject()));
        return SuccessResponse.of(HomeSuccessCode.HOME_RETRIEVED, response);
    }
}
