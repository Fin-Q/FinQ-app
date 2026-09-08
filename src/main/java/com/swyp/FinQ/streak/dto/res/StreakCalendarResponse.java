package com.swyp.FinQ.streak.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "월간 스트릭 조회 응답")
public record StreakCalendarResponse(
        @Schema(description = "조회 월", example = "2026-09")
        String month,
        @Schema(description = "스트릭 인정 날짜 목록", example = "[\"2026-09-01\", \"2026-09-02\"]")
        List<LocalDate> streakDates
) {
}
