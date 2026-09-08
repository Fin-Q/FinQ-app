package com.swyp.FinQ.streak.service;

import com.swyp.FinQ.reward.domain.Level;
import com.swyp.FinQ.reward.dto.info.XpResultInfo;
import com.swyp.FinQ.reward.service.XpGrantService;
import com.swyp.FinQ.streak.domain.StreakCalculator;
import com.swyp.FinQ.streak.dto.info.StreakRecordResult;
import com.swyp.FinQ.streak.repository.StreakLogRepository;
import com.swyp.FinQ.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final StreakLogRepository streakLogRepository;
    private final XpGrantService xpGrantService;
    private final Clock streakClock;

    @Transactional
    public StreakRecordResult recordDailyStreak(User user) {
        LocalDate today = LocalDate.now(streakClock);
        int insertedCount = streakLogRepository.insertIfAbsent(user.getId(), today);

        if (insertedCount == 0) {
            return new StreakRecordResult(false, currentStreakAsOf(user, today), skippedXpResult(user));
        }

        int currentStreak = nextCurrentStreak(user, today);
        user.updateStreak(currentStreak, today);

        int bonusXp = StreakCalculator.calculateBonusXp(currentStreak);
        XpResultInfo bonusXpResult = bonusXp == 0
                ? skippedXpResult(user)
                : xpGrantService.grantStreakBonusXp(user, today, bonusXp);

        return new StreakRecordResult(true, currentStreak, bonusXpResult);
    }

    private int nextCurrentStreak(User user, LocalDate today) {
        if (today.minusDays(1).equals(user.getLastStreakDate())) {
            return user.getCurrentStreak() + 1;
        }
        return 1;
    }

    private int currentStreakAsOf(User user, LocalDate today) {
        LocalDate lastStreakDate = user.getLastStreakDate();
        if (lastStreakDate == null || lastStreakDate.isBefore(today.minusDays(1))) {
            return 0;
        }
        return user.getCurrentStreak();
    }

    private XpResultInfo skippedXpResult(User user) {
        return XpResultInfo.skipped(user.getTotalXp(), Level.from(user.getTotalXp()));
    }
}
