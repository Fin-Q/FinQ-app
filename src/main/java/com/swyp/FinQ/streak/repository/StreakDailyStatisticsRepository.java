package com.swyp.FinQ.streak.repository;

import com.swyp.FinQ.streak.domain.StreakDailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StreakDailyStatisticsRepository extends JpaRepository<StreakDailyStatistics, LocalDate> {

    List<StreakDailyStatistics> findByStatisticsDateBetweenOrderByStatisticsDateAsc(
            LocalDate startDate, LocalDate endDate
    );
}
