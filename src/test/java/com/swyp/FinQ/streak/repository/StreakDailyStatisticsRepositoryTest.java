package com.swyp.FinQ.streak.repository;

import com.swyp.FinQ.streak.domain.StreakDailyStatistics;
import com.swyp.FinQ.support.MySqlContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class StreakDailyStatisticsRepositoryTest extends MySqlContainerSupport {

    @Autowired
    private StreakDailyStatisticsRepository repository;

    @Test
    void storesOneSnapshotPerDateAndReadsInDateOrder() {
        LocalDate date = LocalDate.of(2026, 9, 15);
        repository.saveAndFlush(snapshot(date, 100, 30, 10));
        repository.saveAndFlush(snapshot(date.minusDays(1), 90, 20, 5));
        repository.saveAndFlush(snapshot(date, 110, 40, 15));

        assertThat(repository.findByStatisticsDateBetweenOrderByStatisticsDateAsc(date.minusDays(1), date))
                .extracting(StreakDailyStatistics::getStatisticsDate)
                .containsExactly(date.minusDays(1), date);
        assertThat(repository.findById(date).orElseThrow().getLearnedUserCount()).isEqualTo(110);
    }

    @Test
    void rejectsCountsExceedingPopulation() {
        assertThatThrownBy(() -> repository.saveAndFlush(snapshot(LocalDate.of(2026, 9, 15), 10, 11, 3)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private StreakDailyStatistics snapshot(LocalDate date, long learned, long threeDays, long sevenDays) {
        return StreakDailyStatistics.builder()
                .statisticsDate(date)
                .learnedUserCount(learned)
                .streak3DaysUserCount(threeDays)
                .streak7DaysUserCount(sevenDays)
                .calculatedAt(date.plusDays(1).atTime(0, 10))
                .build();
    }
}
