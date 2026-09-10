package com.swyp.FinQ.streak.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "월간 스트릭 조회 응답")
public record StreakCalendarResponse(
        @Schema(description = "조회 월", example = "2026-09", pattern = "^[0-9]{4}-(0[1-9]|1[0-2])$")
        String month,
        @Schema(description = "스트릭 인정 날짜 목록, 각 값은 YYYY-MM-DD 형식", example = "[\"2026-09-01\", \"2026-09-02\"]")
        List<LocalDate> streakDates
) {
}
