package com.swyp.FinQ.reward.dto.info;

import com.swyp.FinQ.reward.domain.Level;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "XP 부여 결과")
public record XpResultInfo(
        @Schema(description = "획득한 XP", example = "20")
        int xpEarned,
        @Schema(description = "누적 XP", example = "120")
        int totalXp,
        @Schema(description = "이전 레벨", example = "LV1")
        Level previousLevel,
        @Schema(description = "현재 레벨", example = "LV2")
        Level currentLevel,
        @Schema(description = "레벨업 여부", example = "true")
        boolean leveledUp
) {

    public static XpResultInfo skipped(int totalXp, Level currentLevel) {
        return new XpResultInfo(0, totalXp, currentLevel, currentLevel, false);
    }

    public static XpResultInfo granted(int xpEarned, int totalXp, Level previousLevel, Level currentLevel) {
        return new XpResultInfo(xpEarned, totalXp, previousLevel, currentLevel, currentLevel.isHigherThan(previousLevel));
    }
}