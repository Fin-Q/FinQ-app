package com.swyp.FinQ.backoffice.repository;

import com.swyp.FinQ.backoffice.dto.BackofficeStatisticsResponse;
import com.swyp.FinQ.support.MySqlContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BackofficeStatisticsQueryRepositoryTest extends MySqlContainerSupport {

    @Autowired
    private BackofficeStatisticsQueryRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void aggregatesSignupsAndReadsDailyStatistics() {
        insertUser("first@example.com", LocalDateTime.of(2026, 8, 31, 23, 59));
        insertUser("second@example.com", LocalDateTime.of(2026, 9, 1, 10, 0));
        insertUser("third@example.com", LocalDateTime.of(2026, 9, 1, 18, 0));
        jdbcTemplate.update("""
                INSERT INTO streak_daily_statistics
                    (statistics_date, learned_user_count, streak_3_days_user_count,
                     streak_7_days_user_count, calculated_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                LocalDate.of(2026, 9, 1), 30L, 12L, 5L,
                LocalDateTime.of(2026, 9, 2, 0, 5)
        );

        long usersBeforeRange = repository.countUsersBefore(
                LocalDate.of(2026, 9, 1).atStartOfDay()
        );
        List<BackofficeStatisticsResponse.Signup> signups = repository.findDailySignups(
                LocalDate.of(2026, 9, 1).atStartOfDay(),
                LocalDate.of(2026, 9, 2).atStartOfDay()
        );
        List<BackofficeStatisticsResponse.Statistic> statistics =
                repository.findDailyStatistics(
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 1)
                );

        assertThat(usersBeforeRange).isEqualTo(1L);
        assertThat(signups).containsExactly(
                new BackofficeStatisticsResponse.Signup(LocalDate.of(2026, 9, 1), 2L)
        );
        assertThat(statistics).singleElement().satisfies(statistic -> {
            assertThat(statistic.learnedUsers()).isEqualTo(30L);
            assertThat(statistic.streak3Users()).isEqualTo(12L);
            assertThat(statistic.streak7Users()).isEqualTo(5L);
        });
    }

    private void insertUser(String email, LocalDateTime createdAt) {
        jdbcTemplate.update("""
                INSERT INTO users
                    (email, nickname, profile_image_code, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                email, email.substring(0, email.indexOf('@')), "DEFAULT", createdAt, createdAt
        );
    }
}
