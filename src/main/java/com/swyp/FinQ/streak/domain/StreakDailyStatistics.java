package com.swyp.FinQ.streak.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "streak_daily_statistics")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StreakDailyStatistics {

    @Id
    @Column(name = "statistics_date", nullable = false)
    private LocalDate statisticsDate;

    @Column(name = "learned_user_count", nullable = false)
    private long learnedUserCount;

    @Column(name = "streak_3_days_user_count", nullable = false)
    private long streak3DaysUserCount;

    @Column(name = "streak_7_days_user_count", nullable = false)
    private long streak7DaysUserCount;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
}
