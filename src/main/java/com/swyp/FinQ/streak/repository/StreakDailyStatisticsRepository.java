package com.swyp.FinQ.streak.repository;

import com.swyp.FinQ.streak.domain.StreakDailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StreakDailyStatisticsRepository extends JpaRepository<StreakDailyStatistics, LocalDate> {

    @Query(value = """
            WITH learned_users AS (
                SELECT DISTINCT user_id FROM user_content_completion
                WHERE completed_at < :completionCutoff
            ), recent_streaks AS (
                SELECT user_id,
                       SUM(CASE WHEN streak_date >= :threeDaysStart THEN 1 ELSE 0 END) AS three_days,
                       COUNT(*) AS seven_days
                FROM streak_log
                WHERE streak_date BETWEEN :sevenDaysStart AND :statisticsDate
                GROUP BY user_id
            )
            SELECT COUNT(*) AS learnedUserCount,
                   COALESCE(SUM(CASE WHEN s.three_days = 3 THEN 1 ELSE 0 END), 0) AS streak3DaysUserCount,
                   COALESCE(SUM(CASE WHEN s.seven_days = 7 THEN 1 ELSE 0 END), 0) AS streak7DaysUserCount
            FROM learned_users u
            LEFT JOIN recent_streaks s ON s.user_id = u.user_id
            """, nativeQuery = true)
    StreakStatisticsCounts aggregate(
            @Param("statisticsDate") LocalDate statisticsDate,
            @Param("threeDaysStart") LocalDate threeDaysStart,
            @Param("sevenDaysStart") LocalDate sevenDaysStart,
            @Param("completionCutoff") LocalDateTime completionCutoff
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO streak_daily_statistics
                (statistics_date, learned_user_count, streak_3_days_user_count, streak_7_days_user_count, calculated_at)
            VALUES (:statisticsDate, :learnedCount, :threeDaysCount, :sevenDaysCount, :calculatedAt)
            ON DUPLICATE KEY UPDATE
                learned_user_count = :learnedCount,
                streak_3_days_user_count = :threeDaysCount,
                streak_7_days_user_count = :sevenDaysCount,
                calculated_at = :calculatedAt
            """, nativeQuery = true)
    int upsert(
            @Param("statisticsDate") LocalDate statisticsDate,
            @Param("learnedCount") long learnedCount,
            @Param("threeDaysCount") long threeDaysCount,
            @Param("sevenDaysCount") long sevenDaysCount,
            @Param("calculatedAt") LocalDateTime calculatedAt
    );

    List<StreakDailyStatistics> findByStatisticsDateBetweenOrderByStatisticsDateAsc(
            LocalDate startDate, LocalDate endDate
    );
}
