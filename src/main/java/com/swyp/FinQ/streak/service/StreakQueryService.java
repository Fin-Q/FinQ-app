package com.swyp.FinQ.streak.service;

import com.swyp.FinQ.global.exception.BaseException;
import com.swyp.FinQ.streak.domain.StreakCalculator;
import com.swyp.FinQ.streak.domain.StreakLog;
import com.swyp.FinQ.streak.dto.res.StreakCalendarResponse;
import com.swyp.FinQ.streak.dto.res.StreakStatusResponse;
import com.swyp.FinQ.streak.exception.StreakErrorCode;
import com.swyp.FinQ.streak.repository.StreakLogRepository;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.exception.UserErrorCode;
import com.swyp.FinQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StreakQueryService {

    private final UserRepository userRepository;
    private final StreakLogRepository streakLogRepository;
    private final Clock streakClock;

    public StreakStatusResponse getStatus(Long userId) {
        requireUser(userId);
        LocalDate today = LocalDate.now(streakClock);
        List<LocalDate> streakDates = streakLogRepository.findAllByUserIdOrderByStreakDateAsc(userId)
                .stream()
                .map(StreakLog::getStreakDate)
                .toList();

        int currentStreak = StreakCalculator.calculateCurrentStreak(streakDates, today);
        int longestStreak = StreakCalculator.calculateLongestStreak(streakDates);

        return new StreakStatusResponse(
                currentStreak,
                longestStreak,
                StreakCalculator.calculateDaysUntilNextBonus(currentStreak)
        );
    }

    public StreakCalendarResponse getCalendar(Long userId, YearMonth requestedMonth) {
        User user = requireUser(userId);
        YearMonth currentMonth = YearMonth.now(streakClock);
        YearMonth targetMonth = requestedMonth == null ? currentMonth : requestedMonth;
        YearMonth signUpMonth = YearMonth.from(user.getCreatedAt());

        if (targetMonth.isBefore(signUpMonth) || targetMonth.isAfter(currentMonth)) {
            throw BaseException.of(StreakErrorCode.MONTH_OUT_OF_RANGE);
        }

        List<LocalDate> streakDates = streakLogRepository
                .findAllByUserIdAndStreakDateBetweenOrderByStreakDateAsc(
                        userId,
                        targetMonth.atDay(1),
                        targetMonth.atEndOfMonth()
                ).stream()
                .map(StreakLog::getStreakDate)
                .toList();

        return new StreakCalendarResponse(targetMonth.toString(), streakDates);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BaseException.of(UserErrorCode.USER_NOT_FOUND));
    }
}
