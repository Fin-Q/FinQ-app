package com.swyp.FinQ.streak.service;

import com.swyp.FinQ.content.domain.Content;
import com.swyp.FinQ.global.config.JpaAuditingConfig;
import com.swyp.FinQ.learning.domain.UserContentCompletion;
import com.swyp.FinQ.streak.domain.StreakDailyStatistics;
import com.swyp.FinQ.streak.domain.StreakLog;
import com.swyp.FinQ.streak.repository.StreakDailyStatisticsRepository;
import com.swyp.FinQ.support.MySqlContainerSupport;
import com.swyp.FinQ.user.domain.User;
import com.swyp.FinQ.user.domain.ProfileImageCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class StreakStatisticsServiceTest extends MySqlContainerSupport {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 15);
    private static final LocalDateTime CUTOFF = DATE.atTime(15, 0);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StreakDailyStatisticsRepository repository;

    private StreakStatisticsService service;

    @BeforeEach
    void setUp() {
        service = new StreakStatisticsService(repository,
                Clock.fixed(Instant.parse("2026-09-15T15:10:00Z"), ZoneId.of("UTC")));
    }

    @Test
    void countsDistinctLearnersAndOnlyUnbrokenStreaksEndingOnStatisticsDate() {
        User sevenDays = learner("seven", CUTOFF.minusDays(7));
        complete(sevenDays, 2L, CUTOFF.minusSeconds(1));
        streak(sevenDays, DATE.minusDays(6), DATE);
        User threeDays = learner("three", CUTOFF.minusDays(3));
        streak(threeDays, DATE.minusDays(2), DATE);
        User broken = learner("broken", CUTOFF.minusDays(3));
        streak(broken, DATE.minusDays(2), DATE.minusDays(2));
        streak(broken, DATE, DATE);
        User yesterday = learner("yesterday", CUTOFF.minusDays(8));
        streak(yesterday, DATE.minusDays(7), DATE.minusDays(1));
        User notLearned = user("notLearned");
        streak(notLearned, DATE.minusDays(6), DATE);
        streak(threeDays, DATE.plusDays(1), DATE.plusDays(1));
        entityManager.flush();

        service.aggregate(DATE);

        assertCounts(4, 2, 1);
    }

    @Test
    void convertsKstMidnightToExclusiveUtcCompletionCutoff() {
        learner("before", CUTOFF.minusSeconds(1));
        learner("at", CUTOFF);
        learner("after", CUTOFF.plusSeconds(1));
        entityManager.flush();

        service.aggregate(DATE);

        assertCounts(1, 0, 0);
        assertThat(repository.findById(DATE).orElseThrow().getCalculatedAt())
                .isEqualTo(DATE.plusDays(1).atTime(0, 10));
    }

    @Test
    void storesZeroPopulationAndUpdatesSameDateWithoutDuplicateSnapshot() {
        service.aggregate(DATE);
        assertCounts(0, 0, 0);
        User added = learner("added", CUTOFF.minusDays(3));
        streak(added, DATE.minusDays(2), DATE);
        entityManager.flush();

        service.aggregate(DATE);

        assertCounts(1, 1, 0);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void rejectsTodayAndFutureDatesUsingKstEvenWhenClockIsUtc() {
        assertThatThrownBy(() -> service.aggregate(DATE.plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.aggregate(DATE.plusDays(2)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(repository.count()).isZero();
    }

    private User user(String name) {
        User user = User.builder().email(name + "@example.com").nickname(name)
                .profileImageCode(ProfileImageCode.PROFILE_01).build();
        entityManager.persist(user);
        return user;
    }

    private User learner(String name, LocalDateTime completedAt) {
        User user = user(name);
        complete(user, 1L, completedAt);
        return user;
    }

    private void complete(User user, Long contentId, LocalDateTime completedAt) {
        entityManager.persist(UserContentCompletion.builder()
                .user(user).content(entityManager.getReference(Content.class, contentId))
                .completedAt(completedAt).xpEarned(10).build());
    }

    private void streak(User user, LocalDate start, LocalDate end) {
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            entityManager.persist(StreakLog.builder().user(user).streakDate(date).build());
        }
    }

    private void assertCounts(long learned, long threeDays, long sevenDays) {
        StreakDailyStatistics snapshot = repository.findById(DATE).orElseThrow();
        assertThat(snapshot.getLearnedUserCount()).isEqualTo(learned);
        assertThat(snapshot.getStreak3DaysUserCount()).isEqualTo(threeDays);
        assertThat(snapshot.getStreak7DaysUserCount()).isEqualTo(sevenDays);
    }
}
