CREATE TABLE streak_daily_statistics (
    statistics_date DATE NOT NULL,
    learned_user_count BIGINT NOT NULL,
    streak_3_days_user_count BIGINT NOT NULL,
    streak_7_days_user_count BIGINT NOT NULL,
    calculated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (statistics_date),
    CONSTRAINT chk_streak_statistics_counts CHECK (
        learned_user_count >= 0
        AND streak_3_days_user_count >= streak_7_days_user_count
        AND streak_7_days_user_count >= 0
        AND learned_user_count >= streak_3_days_user_count
    )
);
