package com.swyp.FinQ.streak.repository;

import com.swyp.FinQ.streak.domain.StreakLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StreakLogRepository extends JpaRepository<StreakLog, Long> {

    boolean existsByUserIdAndStreakDate(Long userId, LocalDate streakDate);

    List<StreakLog> findAllByUserIdAndStreakDateBetweenOrderByStreakDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<StreakLog> findAllByUserIdOrderByStreakDateAsc(Long userId);
}
