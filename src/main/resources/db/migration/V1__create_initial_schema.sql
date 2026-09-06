-- MARK: 콘텐츠

CREATE TABLE category (
    category_id BIGINT NOT NULL AUTO_INCREMENT,
    category_code VARCHAR(50) NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_category PRIMARY KEY (category_id),
    CONSTRAINT uk_category_code UNIQUE (category_code)
);

CREATE TABLE content (
    content_id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    content_code VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(500) NULL,
    source VARCHAR(200) NULL,
    reference_date DATE NULL,
    display_order INT NOT NULL,
    body_data JSON NULL,
    summary_content TEXT NULL,
    is_premium TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_content PRIMARY KEY (content_id),
    CONSTRAINT uk_content_code UNIQUE (content_code),
    CONSTRAINT fk_content_category FOREIGN KEY (category_id) REFERENCES category (category_id),
    INDEX idx_content_category (category_id)
);

CREATE TABLE content_question (
    content_question_id BIGINT NOT NULL AUTO_INCREMENT,
    content_id BIGINT NOT NULL,
    question_code VARCHAR(50) NOT NULL,
    content_stage VARCHAR(5) NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    question_body TEXT NOT NULL,
    explanation TEXT NOT NULL,
    option_a VARCHAR(500) NOT NULL,
    option_b VARCHAR(500) NOT NULL,
    option_c VARCHAR(500) NULL,
    option_d VARCHAR(500) NULL,
    correct_answer VARCHAR(1) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_content_question PRIMARY KEY (content_question_id),
    CONSTRAINT uk_content_question_code UNIQUE (question_code),
    CONSTRAINT fk_content_question_content FOREIGN KEY (content_id) REFERENCES content (content_id),
    INDEX idx_content_question_content (content_id)
);

-- MARK: 학습

CREATE TABLE advanced_quiz (
    advanced_quiz_id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    question_code VARCHAR(50) NOT NULL,
    quiz_order INT NOT NULL,
    question_body TEXT NOT NULL,
    explanation TEXT NOT NULL,
    option_a VARCHAR(500) NOT NULL,
    option_b VARCHAR(500) NOT NULL,
    option_c VARCHAR(500) NOT NULL,
    option_d VARCHAR(500) NOT NULL,
    correct_answer VARCHAR(1) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_advanced_quiz PRIMARY KEY (advanced_quiz_id),
    CONSTRAINT uk_advanced_quiz_code UNIQUE (question_code),
    CONSTRAINT uk_category_order UNIQUE (category_id, quiz_order),
    CONSTRAINT fk_advanced_quiz_category FOREIGN KEY (category_id) REFERENCES category (category_id)
);

CREATE TABLE user_content_completion (
    user_content_completion_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    content_id BIGINT NOT NULL,
    completed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    xp_earned INT NOT NULL DEFAULT 10,
    CONSTRAINT pk_user_content_completion PRIMARY KEY (user_content_completion_id),
    CONSTRAINT uk_user_content UNIQUE (user_id, content_id),
    CONSTRAINT fk_user_content_completion_content FOREIGN KEY (content_id) REFERENCES content (content_id),
    INDEX idx_user_content_completion_user_id (user_id)
);

CREATE TABLE user_category_completion (
    user_category_completion_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    completed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    xp_earned INT NOT NULL DEFAULT 30,
    CONSTRAINT pk_user_category_completion PRIMARY KEY (user_category_completion_id),
    CONSTRAINT uk_user_category UNIQUE (user_id, category_id),
    CONSTRAINT fk_user_category_completion_category FOREIGN KEY (category_id) REFERENCES category (category_id),
    INDEX idx_user_category_completion_user_id (user_id)
);

-- MARK: 리워드

CREATE TABLE xp_history (
    xp_history_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    xp_amount INT NOT NULL,
    xp_type VARCHAR(30) NOT NULL,
    reference_id VARCHAR(100) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_xp_history PRIMARY KEY (xp_history_id),
    CONSTRAINT uk_xp_history_user_type_ref UNIQUE (user_id, xp_type, reference_id),
    INDEX idx_xp_history_user (user_id)
);
