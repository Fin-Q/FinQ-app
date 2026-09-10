package com.swyp.FinQ.streak.controller;

import com.swyp.FinQ.global.success.SuccessResponse;
import com.swyp.FinQ.global.config.ApiDocumentation;
import com.swyp.FinQ.global.config.ApiOwner;
import com.swyp.FinQ.global.config.ApiTags;
import com.swyp.FinQ.streak.dto.res.StreakCalendarResponse;
import com.swyp.FinQ.streak.dto.res.StreakStatusResponse;
import com.swyp.FinQ.streak.service.StreakQueryService;
import com.swyp.FinQ.streak.success.StreakSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@Tag(name = ApiTags.STREAK, description = "스트릭 기록 조회 API")
@RestController
@RequestMapping("/streak")
@RequiredArgsConstructor
public class StreakController {

    private final StreakQueryService streakQueryService;

    @Operation(summary = "현재 스트릭 상태 조회", description = "현재 스트릭과 다음 보너스까지 남은 일수를 조회합니다.")
    @ApiDocumentation(
            id = "STREAK-002", name = "현재 스트릭 요약 조회", owner = ApiOwner.MINJI,
            errors = "USER_NOT_FOUND"
    )
    @GetMapping("/status")
    public ResponseEntity<SuccessResponse<StreakStatusResponse>> getStatus(
            @AuthenticationPrincipal Jwt jwt
    ) {
        StreakStatusResponse response = streakQueryService.getStatus(Long.valueOf(jwt.getSubject()));
        return SuccessResponse.of(StreakSuccessCode.STATUS_RETRIEVED, response);
    }

    @Operation(summary = "월간 스트릭 조회", description = "가입 월부터 현재 월까지의 스트릭 인정 날짜를 조회합니다.")
    @ApiDocumentation(
            id = "STREAK-001", name = "월간 스트릭 조회", owner = ApiOwner.MINJI,
            errors = {"USER_NOT_FOUND", "STREAK_MONTH_OUT_OF_RANGE"}
    )
    @GetMapping("/calendar")
    public ResponseEntity<SuccessResponse<StreakCalendarResponse>> getCalendar(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "조회 월(생략 시 현재 월)", example = "2026-09")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        StreakCalendarResponse response = streakQueryService.getCalendar(Long.valueOf(jwt.getSubject()), month);
        return SuccessResponse.of(StreakSuccessCode.CALENDAR_RETRIEVED, response);
    }
}
