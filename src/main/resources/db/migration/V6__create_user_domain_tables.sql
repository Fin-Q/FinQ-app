-- MARK: 사용자

CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    nickname VARCHAR(50) NOT NULL,
    profile_image_code VARCHAR(50) NOT NULL,
    onboarding_status VARCHAR(30) NOT NULL DEFAULT 'INTEREST_SELECTION',
    onboarding_completed_at DATETIME(6) NULL,
    notification_enabled TINYINT(1) NOT NULL DEFAULT 1,
    total_xp INT NOT NULL DEFAULT 0,
    current_streak INT NOT NULL DEFAULT 0,
    last_streak_date DATE NULL,
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_agreement (
    user_agreement_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    agreement_code VARCHAR(50) NOT NULL,
    agreement_version VARCHAR(20) NOT NULL,
    agreed TINYINT(1) NOT NULL,
    agreed_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_user_agreement PRIMARY KEY (user_agreement_id),
    CONSTRAINT uk_user_agreement UNIQUE (user_id, agreement_code, agreement_version),
    CONSTRAINT fk_user_agreement_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    INDEX idx_user_agreement_code_version (agreement_code, agreement_version)
);

-- MARK: 소셜 계정

CREATE TABLE social_account (
    social_account_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    linked_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_social_account PRIMARY KEY (social_account_id),
    CONSTRAINT uk_social_account_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT uk_social_account_user_provider UNIQUE (user_id, provider),
    CONSTRAINT fk_social_account_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- MARK: 인증 토큰

CREATE TABLE refresh_token (
    refresh_token_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_refresh_token PRIMARY KEY (refresh_token_id),
    CONSTRAINT uk_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    INDEX idx_refresh_token_user (user_id),
    INDEX idx_refresh_token_expires_at (expires_at)
);

CREATE TABLE password_reset_request (
    password_reset_request_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    verification_id VARCHAR(64) NOT NULL,
    verification_code_hash VARCHAR(255) NOT NULL,
    code_expires_at DATETIME(6) NOT NULL,
    resend_available_at DATETIME(6) NOT NULL,
    failed_attempt_count INT NOT NULL DEFAULT 0,
    verified_at DATETIME(6) NULL,
    password_reset_token_hash CHAR(64) NULL,
    token_expires_at DATETIME(6) NULL,
    consumed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_password_reset_request PRIMARY KEY (password_reset_request_id),
    CONSTRAINT uk_password_reset_verification_id UNIQUE (verification_id),
    CONSTRAINT uk_password_reset_token_hash UNIQUE (password_reset_token_hash),
    CONSTRAINT fk_password_reset_request_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    INDEX idx_password_reset_request_user (user_id),
    INDEX idx_password_reset_request_code_expiry (code_expires_at),
    INDEX idx_password_reset_request_token_expiry (token_expires_at)
);

-- MARK: 관심 주제

CREATE TABLE user_interest (
    user_interest_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_user_interest PRIMARY KEY (user_interest_id),
    CONSTRAINT uk_user_interest UNIQUE (user_id, category_id),
    CONSTRAINT fk_user_interest_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_user_interest_category FOREIGN KEY (category_id) REFERENCES category (category_id),
    INDEX idx_user_interest_category (category_id)
);

-- MARK: 기존 도메인 사용자 관계

ALTER TABLE user_content_completion
    ADD CONSTRAINT fk_user_content_completion_user
        FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE user_category_completion
    ADD CONSTRAINT fk_user_category_completion_user
        FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE xp_history
    ADD CONSTRAINT fk_xp_history_user
        FOREIGN KEY (user_id) REFERENCES users (user_id);
