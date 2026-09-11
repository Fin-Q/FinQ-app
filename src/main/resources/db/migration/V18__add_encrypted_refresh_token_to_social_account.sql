ALTER TABLE social_account
    ADD COLUMN encrypted_refresh_token VARCHAR(2048) NULL AFTER provider_user_id;
