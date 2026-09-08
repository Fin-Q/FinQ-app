CREATE TABLE push_token (
    push_token_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(36) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    fcm_token VARCHAR(2048) NOT NULL,
    fcm_token_hash CHAR(64) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_push_token PRIMARY KEY (push_token_id),
    CONSTRAINT uk_push_token_device_id UNIQUE (device_id),
    CONSTRAINT uk_push_token_fcm_token_hash UNIQUE (fcm_token_hash),
    CONSTRAINT fk_push_token_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    INDEX idx_push_token_user (user_id),
    INDEX idx_push_token_user_session (user_id, session_id)
);
