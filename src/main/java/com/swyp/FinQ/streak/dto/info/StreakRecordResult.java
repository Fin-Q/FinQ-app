package com.swyp.FinQ.streak.dto.info;

import com.swyp.FinQ.reward.dto.info.XpResultInfo;

public record StreakRecordResult(
        boolean recorded,
        int currentStreak,
        XpResultInfo bonusXpResult
) {
}
