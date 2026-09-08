package com.swyp.FinQ.streak.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 스트릭 상태 조회 응답")
public record StreakStatusResponse(
        @Schema(description = "현재 연속 학습 일수", example = "3")
        int currentStreak,
        @Schema(description = "최장 연속 학습 일수", example = "10")
        int longestStreak,
        @Schema(description = "다음 스트릭 보너스까지 남은 일수", example = "2")
        int daysUntilNextBonus
) {
}
