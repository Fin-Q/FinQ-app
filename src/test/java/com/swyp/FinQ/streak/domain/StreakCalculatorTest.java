package com.swyp.FinQ.streak.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StreakCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 8);

    @Test
    @DisplayName("오늘 또는 어제까지 이어진 연속 기록으로 현재 스트릭을 계산한다")
    void calculatesCurrentStreak() {
        List<LocalDate> streakDates = List.of(
                TODAY.minusDays(4),
                TODAY.minusDays(2),
                TODAY.minusDays(1),
                TODAY
        );

        assertThat(StreakCalculator.calculateCurrentStreak(streakDates, TODAY)).isEqualTo(3);
        assertThat(StreakCalculator.calculateCurrentStreak(
                List.of(TODAY.minusDays(3), TODAY.minusDays(2), TODAY.minusDays(1)),
                TODAY
        )).isEqualTo(3);
    }

    @Test
    @DisplayName("마지막 기록이 어제보다 이전이면 현재 스트릭은 0이다")
    void resetsExpiredCurrentStreak() {
        assertThat(StreakCalculator.calculateCurrentStreak(
                List.of(TODAY.minusDays(3), TODAY.minusDays(2)),
                TODAY
        )).isZero();
    }

    @Test
    @DisplayName("다음 5일 단위 보너스까지 남은 일수를 계산한다")
    void calculatesDaysUntilNextBonus() {
        assertThat(StreakCalculator.calculateDaysUntilNextBonus(0)).isEqualTo(5);
        assertThat(StreakCalculator.calculateDaysUntilNextBonus(3)).isEqualTo(2);
        assertThat(StreakCalculator.calculateDaysUntilNextBonus(5)).isEqualTo(5);
    }

    @Test
    @DisplayName("5·10일에는 5XP, 15일 이후 5일 단위에는 10XP를 계산한다")
    void calculatesBonusXp() {
        assertThat(StreakCalculator.calculateBonusXp(4)).isZero();
        assertThat(StreakCalculator.calculateBonusXp(5)).isEqualTo(5);
        assertThat(StreakCalculator.calculateBonusXp(10)).isEqualTo(5);
        assertThat(StreakCalculator.calculateBonusXp(15)).isEqualTo(10);
        assertThat(StreakCalculator.calculateBonusXp(25)).isEqualTo(10);
    }
}
