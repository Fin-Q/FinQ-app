package com.swyp.FinQ.reward.dto.info;

import com.swyp.FinQ.reward.domain.Level;

public record XpResultInfo(
        int xpEarned,
        int totalXp,
        Level previousLevel,
        Level currentLevel,
        boolean leveledUp
) {

    public static XpResultInfo skipped(int totalXp, Level currentLevel) {
        return new XpResultInfo(0, totalXp, currentLevel, currentLevel, false);
    }

    public static XpResultInfo granted(int xpEarned, int totalXp, Level previousLevel, Level currentLevel) {
        return new XpResultInfo(xpEarned, totalXp, previousLevel, currentLevel, currentLevel.isHigherThan(previousLevel));
    }
}