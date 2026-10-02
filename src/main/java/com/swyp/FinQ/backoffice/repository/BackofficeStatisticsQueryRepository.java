package com.swyp.FinQ.backoffice.repository;

import com.swyp.FinQ.backoffice.dto.BackofficeStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BackofficeStatisticsQueryRepository {

    private final JdbcClient jdbcClient;

    public long countUsersBefore(LocalDateTime rangeStart) {
        return jdbcClient.sql("SELECT COUNT(*) FROM users WHERE created_at < :rangeStart")
                .param("rangeStart", rangeStart)
                .query(Long.class)
                .single();
    }

    public List<BackofficeStatisticsResponse.Signup> findDailySignups(
            LocalDateTime rangeStart,
            LocalDateTime rangeEndExclusive
    ) {
        return jdbcClient.sql("""
                        SELECT DATE(created_at) AS signup_date, COUNT(*) AS signup_count
                        FROM users
                        WHERE created_at >= :rangeStart
                          AND created_at < :rangeEndExclusive
                        GROUP BY DATE(created_at)
                        ORDER BY signup_date
                        """)
                .param("rangeStart", rangeStart)
                .param("rangeEndExclusive", rangeEndExclusive)
                .query((resultSet, rowNumber) -> new BackofficeStatisticsResponse.Signup(
                        resultSet.getDate("signup_date").toLocalDate(),
                        resultSet.getLong("signup_count")
                ))
                .list();
    }

    public List<BackofficeStatisticsResponse.Statistic> findDailyStatistics(
            LocalDate from,
            LocalDate to
    ) {
        return jdbcClient.sql("""
                        SELECT statistics_date,
                               learned_user_count,
                               streak_3_days_user_count,
                               streak_7_days_user_count,
                               calculated_at
                        FROM streak_daily_statistics
                        WHERE statistics_date BETWEEN :from AND :to
                        ORDER BY statistics_date
                        """)
                .param("from", from)
                .param("to", to)
                .query((resultSet, rowNumber) -> new BackofficeStatisticsResponse.Statistic(
                        resultSet.getDate("statistics_date").toLocalDate(),
                        resultSet.getLong("learned_user_count"),
                        resultSet.getLong("streak_3_days_user_count"),
                        resultSet.getLong("streak_7_days_user_count"),
                        resultSet.getTimestamp("calculated_at").toLocalDateTime()
                ))
                .list();
    }
}
