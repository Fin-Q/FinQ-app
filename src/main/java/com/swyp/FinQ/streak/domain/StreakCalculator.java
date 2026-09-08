package com.swyp.FinQ.streak.domain;

import com.swyp.FinQ.reward.domain.XpConstants;

import java.time.LocalDate;
import java.util.List;

public final class StreakCalculator {

    private static final int BONUS_INTERVAL_DAYS = 5;

    private StreakCalculator() {
    }

    public static int calculateCurrentStreak(List<LocalDate> streakDates, LocalDate today) {
        if (streakDates.isEmpty()) {
            return 0;
        }

        List<LocalDate> sortedDates = streakDates.stream()
                .distinct()
                .sorted()
                .toList();
        LocalDate latestDate = sortedDates.getLast();

        if (latestDate.isBefore(today.minusDays(1))) {
            return 0;
        }

        int currentStreak = 1;
        for (int index = sortedDates.size() - 1; index > 0; index--) {
            if (!sortedDates.get(index - 1).equals(sortedDates.get(index).minusDays(1))) {
                break;
            }
            currentStreak++;
        }
        return currentStreak;
    }

    public static int calculateDaysUntilNextBonus(int currentStreak) {
        int remainder = currentStreak % BONUS_INTERVAL_DAYS;
        return remainder == 0 ? BONUS_INTERVAL_DAYS : BONUS_INTERVAL_DAYS - remainder;
    }

    public static int calculateBonusXp(int currentStreak) {
        if (currentStreak <= 0 || currentStreak % BONUS_INTERVAL_DAYS != 0) {
            return 0;
        }
        if (currentStreak <= 10) {
            return XpConstants.EARLY_STREAK_BONUS_XP;
        }
        return XpConstants.REGULAR_STREAK_BONUS_XP;
    }
}
