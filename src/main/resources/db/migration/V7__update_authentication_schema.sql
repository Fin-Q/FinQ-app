-- MARK: 온보딩 최초 상태

UPDATE users
SET onboarding_status = 'INTEREST_SECTION'
WHERE onboarding_status = 'INTEREST_SELECTION';

ALTER TABLE users
    MODIFY COLUMN onboarding_status VARCHAR(30) NOT NULL DEFAULT 'INTEREST_SECTION';

-- MARK: 리프레시 토큰 보호

ALTER TABLE refresh_token
    ADD COLUMN session_id CHAR(36) NULL AFTER user_id,
    ADD COLUMN token_hash CHAR(64) NULL AFTER token;

UPDATE refresh_token
SET session_id = UUID(),
    token_hash = SHA2(token, 256);

ALTER TABLE refresh_token
    DROP INDEX uk_refresh_token_token,
    DROP COLUMN token,
    MODIFY COLUMN session_id CHAR(36) NOT NULL,
    MODIFY COLUMN token_hash CHAR(64) NOT NULL,
    ADD CONSTRAINT uk_refresh_token_session_id UNIQUE (session_id),
    ADD CONSTRAINT uk_refresh_token_token_hash UNIQUE (token_hash);
