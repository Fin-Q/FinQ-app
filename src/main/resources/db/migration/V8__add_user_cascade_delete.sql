-- MARK: 사용자 종속 데이터 연쇄 삭제

ALTER TABLE user_agreement
    DROP FOREIGN KEY fk_user_agreement_user;
ALTER TABLE user_agreement
    ADD CONSTRAINT fk_user_agreement_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE social_account
    DROP FOREIGN KEY fk_social_account_user;
ALTER TABLE social_account
    ADD CONSTRAINT fk_social_account_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE refresh_token
    DROP FOREIGN KEY fk_refresh_token_user;
ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE password_reset_request
    DROP FOREIGN KEY fk_password_reset_request_user;
ALTER TABLE password_reset_request
    ADD CONSTRAINT fk_password_reset_request_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE user_interest
    DROP FOREIGN KEY fk_user_interest_user;
ALTER TABLE user_interest
    ADD CONSTRAINT fk_user_interest_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE user_content_completion
    DROP FOREIGN KEY fk_user_content_completion_user;
ALTER TABLE user_content_completion
    ADD CONSTRAINT fk_user_content_completion_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE user_category_completion
    DROP FOREIGN KEY fk_user_category_completion_user;
ALTER TABLE user_category_completion
    ADD CONSTRAINT fk_user_category_completion_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;

ALTER TABLE xp_history
    DROP FOREIGN KEY fk_xp_history_user;
ALTER TABLE xp_history
    ADD CONSTRAINT fk_xp_history_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE;
