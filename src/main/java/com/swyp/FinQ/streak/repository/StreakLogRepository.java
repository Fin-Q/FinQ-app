package com.swyp.FinQ.streak.repository;

import com.swyp.FinQ.streak.domain.StreakLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StreakLogRepository extends JpaRepository<StreakLog, Long> {

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT IGNORE INTO streak_log (user_id, streak_date, created_at)
            VALUES (:userId, :streakDate, CURRENT_TIMESTAMP(6))
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("userId") Long userId,
            @Param("streakDate") LocalDate streakDate
    );

    boolean existsByUserIdAndStreakDate(Long userId, LocalDate streakDate);

    List<StreakLog> findAllByUserIdAndStreakDateBetweenOrderByStreakDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<StreakLog> findAllByUserIdOrderByStreakDateAsc(Long userId);
}
