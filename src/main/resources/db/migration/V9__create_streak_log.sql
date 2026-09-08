-- MARK: 스트릭 기록

CREATE TABLE streak_log (
    streak_log_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    streak_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_streak_log PRIMARY KEY (streak_log_id),
    CONSTRAINT uk_streak_log_user_date UNIQUE (user_id, streak_date),
    CONSTRAINT fk_streak_log_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);
