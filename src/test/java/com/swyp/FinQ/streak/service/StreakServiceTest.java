package com.swyp.FinQ.streak.service;

import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.reward.dto.info.XpResultInfo;
import com.swyp.FinQ.reward.service.XpGrantService;
import com.swyp.FinQ.streak.dto.info.StreakRecordResult;
import com.swyp.FinQ.streak.repository.StreakLogRepository;
import com.swyp.FinQ.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 8);

    @Mock
    private StreakLogRepository streakLogRepository;

    @Mock
    private XpGrantService xpGrantService;

    private StreakService streakService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-09-07T15:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        streakService = new StreakService(streakLogRepository, xpGrantService, fixedClock);
    }

    @Test
    @DisplayName("첫 스트릭을 KST 날짜로 기록한다")
    void recordsFirstDailyStreak() {
        User user = createUser(0, null, 20);
        given(streakLogRepository.insertIfAbsent(1L, TODAY)).willReturn(1);

        StreakRecordResult result = streakService.recordDailyStreak(user);

        assertThat(result.recorded()).isTrue();
        assertThat(result.currentStreak()).isEqualTo(1);
        assertThat(result.bonusXpResult().xpEarned()).isZero();
        assertThat(user.getCurrentStreak()).isEqualTo(1);
        assertThat(user.getLastStreakDate()).isEqualTo(TODAY);
        verify(xpGrantService, never()).grantStreakBonusXp(user, TODAY, 5);
    }

    @Test
    @DisplayName("어제까지 4일 연속이면 오늘 5일 스트릭과 5XP를 지급한다")
    void grantsEarlyStreakBonus() {
        User user = createUser(4, TODAY.minusDays(1), 70);
        XpResultInfo xpResult = XpResultInfo.granted(5, 75, Level.LV1, Level.LV1);
        given(streakLogRepository.insertIfAbsent(1L, TODAY)).willReturn(1);
        given(xpGrantService.grantStreakBonusXp(user, TODAY, 5)).willReturn(xpResult);

        StreakRecordResult result = streakService.recordDailyStreak(user);

        assertThat(result.currentStreak()).isEqualTo(5);
        assertThat(result.bonusXpResult().xpEarned()).isEqualTo(5);
        verify(xpGrantService).grantStreakBonusXp(user, TODAY, 5);
    }

    @Test
    @DisplayName("어제 기록이 없으면 오늘 스트릭을 1일부터 다시 시작한다")
    void restartsStreakAfterGap() {
        User user = createUser(8, TODAY.minusDays(2), 100);
        given(streakLogRepository.insertIfAbsent(1L, TODAY)).willReturn(1);

        StreakRecordResult result = streakService.recordDailyStreak(user);

        assertThat(result.currentStreak()).isEqualTo(1);
        assertThat(user.getCurrentStreak()).isEqualTo(1);
        verify(xpGrantService, never()).grantStreakBonusXp(user, TODAY, 10);
    }

    @Test
    @DisplayName("오늘 기록이 이미 존재하면 스트릭과 보너스를 중복 처리하지 않는다")
    void skipsDuplicateDailyStreak() {
        User user = createUser(5, TODAY, 75);
        given(streakLogRepository.insertIfAbsent(1L, TODAY)).willReturn(0);

        StreakRecordResult result = streakService.recordDailyStreak(user);

        assertThat(result.recorded()).isFalse();
        assertThat(result.currentStreak()).isEqualTo(5);
        assertThat(result.bonusXpResult().xpEarned()).isZero();
        verify(xpGrantService, never()).grantStreakBonusXp(user, TODAY, 5);
    }

    private User createUser(int currentStreak, LocalDate lastStreakDate, int totalXp) {
        return User.builder()
                .id(1L)
                .email("streak@example.com")
                .nickname("Minter")
                .totalXp(totalXp)
                .currentStreak(currentStreak)
                .lastStreakDate(lastStreakDate)
                .build();
    }
}
