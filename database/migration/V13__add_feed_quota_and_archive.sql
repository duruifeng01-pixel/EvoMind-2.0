-- V13__add_feed_quota_and_archive.sql
-- 添加每日Feed配额表和归档相关字段

-- 用户每日Feed配额表
CREATE TABLE IF NOT EXISTS user_daily_feed_quotas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    quota_date DATE NOT NULL,
    daily_limit INT NOT NULL DEFAULT 300,
    consumed_count INT NOT NULL DEFAULT 0,
    remaining_count INT NOT NULL DEFAULT 300,
    user_source_count INT DEFAULT 0,
    recommended_count INT DEFAULT 0,
    is_exhausted BOOLEAN DEFAULT FALSE,
    exhausted_at TIMESTAMP,
    last_reset_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, quota_date),
    INDEX idx_user_id (user_id),
    INDEX idx_quota_date (quota_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户每日Feed配额表';

-- 已读内容防重复记录表（记录用户已读内容，3天内不重复推荐）
CREATE TABLE IF NOT EXISTS user_read_card_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    first_read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMP,
    read_count INT DEFAULT 1,
    cool_down_until TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_card (user_id, card_id),
    INDEX idx_user_id (user_id),
    INDEX idx_cool_down (cool_down_until),
    INDEX idx_card_id (card_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户已读内容防重复记录';

-- 添加卡片归档相关字段
ALTER TABLE cards 
ADD COLUMN IF NOT EXISTS is_archived BOOLEAN DEFAULT FALSE COMMENT '是否已归档',
ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP COMMENT '归档时间',
ADD COLUMN IF NOT EXISTS archive_reason VARCHAR(50) COMMENT '归档原因(OLD/READ/LOW_QUALITY/USER_ACTION)';

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_cards_archived ON cards(is_archived, created_at);
CREATE INDEX IF NOT EXISTS idx_cards_archive_reason ON cards(archive_reason);
