-- V11: 添加Feed流推荐相关表
-- 用于实现7:3智能混合信息流

-- 用户兴趣画像表
CREATE TABLE IF NOT EXISTS user_interest_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    interest_tag VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    weight DECIMAL(5, 4) DEFAULT 0.5000,
    source_count INT DEFAULT 0,
    interaction_score INT DEFAULT 0,
    last_interaction_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_tag (user_id, interest_tag),
    INDEX idx_user_id (user_id),
    INDEX idx_interest_tag (interest_tag),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户阅读历史表
CREATE TABLE IF NOT EXISTS user_reading_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    source_id BIGINT,
    read_duration_seconds INT DEFAULT 0,
    read_percentage INT DEFAULT 0,
    is_favorite BOOLEAN DEFAULT FALSE,
    is_archived BOOLEAN DEFAULT FALSE,
    interaction_type VARCHAR(20) DEFAULT 'VIEW',
    keywords VARCHAR(500),
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_card (user_id, card_id),
    INDEX idx_user_id (user_id),
    INDEX idx_card_id (card_id),
    INDEX idx_read_at (read_at),
    INDEX idx_interaction_type (interaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 添加注释
ALTER TABLE user_interest_profiles COMMENT = '用户兴趣画像表，记录用户的兴趣标签和权重';
ALTER TABLE user_reading_histories COMMENT = '用户阅读历史表，记录用户的阅读行为';
