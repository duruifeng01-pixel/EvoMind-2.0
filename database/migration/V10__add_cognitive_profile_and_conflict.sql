-- V10: 添加用户认知画像和认知冲突表
-- 用于实现"新卡片vs用户语料库"的观点冲突检测

-- 用户认知画像表
CREATE TABLE IF NOT EXISTS user_cognitive_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    topic VARCHAR(100) NOT NULL,
    core_belief VARCHAR(1000),
    belief_type VARCHAR(50),
    confidence_level DECIMAL(3, 2),
    evidence_count INT DEFAULT 0,
    source_card_ids VARCHAR(500),
    keywords VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_topic (user_id, topic),
    INDEX idx_user_id (user_id),
    INDEX idx_topic (topic)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 认知冲突表（新卡片vs用户认知画像）
CREATE TABLE IF NOT EXISTS cognitive_conflicts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    conflict_type VARCHAR(50),
    conflict_description VARCHAR(500),
    user_belief VARCHAR(1000),
    card_viewpoint VARCHAR(1000),
    conflict_score DECIMAL(5, 4),
    is_acknowledged BOOLEAN DEFAULT FALSE,
    is_dismissed BOOLEAN DEFAULT FALSE,
    ai_analysis VARCHAR(2000),
    topic VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_card_id (card_id),
    INDEX idx_profile_id (profile_id),
    INDEX idx_user_acknowledged (user_id, is_acknowledged, is_dismissed),
    UNIQUE KEY uk_card_profile (card_id, profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 添加注释
COMMENT ON TABLE user_cognitive_profiles IS '用户认知画像表，记录用户的核心认知立场';
COMMENT ON TABLE cognitive_conflicts IS '认知冲突表，记录新卡片与用户认知体系的冲突';
