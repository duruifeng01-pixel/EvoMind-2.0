-- ============================================
-- 用户语料库表 (user_corpus)
-- 存储用户生成的知识资产，不参与 Feed 流推荐
-- 与 cards 表的区别：
--   - cards: 来自外部信息源，参与 7:3 Feed 流推荐
--   - user_corpus: 用户自己生成的内容，是用户的知识资产
-- ============================================

CREATE TABLE IF NOT EXISTS user_corpus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content_text LONGTEXT COMMENT '内容文本',
    summary_text VARCHAR(1000) COMMENT '摘要',
    one_sentence_summary VARCHAR(200) COMMENT '一句话导读',
    corpus_type VARCHAR(30) NOT NULL DEFAULT 'SOCRATIC_INSIGHT' COMMENT '语料类型: SOCRATIC_INSIGHT, USER_NOTE, HIGHLIGHT, AI_SUMMARY, REFLECTION, INSIGHT',
    source_type VARCHAR(30) COMMENT '来源类型: SOCRATIC_DIALOGUE, DISCUSSION, CARD, MANUAL_INPUT, VOICE_NOTE, OCR_IMPORT, LINK_SCRAPE',
    source_id BIGINT COMMENT '关联来源ID',
    source_ref VARCHAR(512) COMMENT '关联来源标识/URL',
    discussion_id BIGINT COMMENT '关联的讨论ID',
    keywords VARCHAR(500) COMMENT '关键词标签，逗号分隔',
    reading_time_minutes INT COMMENT '阅读时长（分钟）',
    is_favorite TINYINT(1) DEFAULT 0 COMMENT '是否收藏',
    is_pinned TINYINT(1) DEFAULT 0 COMMENT '是否置顶',
    pinned_at DATETIME COMMENT '置顶时间',
    is_archived TINYINT(1) DEFAULT 0 COMMENT '是否归档',
    archived_at DATETIME COMMENT '归档时间',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    last_viewed_at DATETIME COMMENT '最后查看时间',
    related_card_id BIGINT COMMENT '关联的原始卡片ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_user_archived (user_id, is_archived),
    INDEX idx_user_favorite (user_id, is_favorite),
    INDEX idx_user_pinned (user_id, is_pinned),
    INDEX idx_corpus_type (corpus_type),
    INDEX idx_source (source_type, source_id),
    INDEX idx_discussion (discussion_id),
    INDEX idx_keywords (keywords),
    
    -- 外键约束
    CONSTRAINT fk_corpus_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_corpus_discussion FOREIGN KEY (discussion_id) REFERENCES discussions(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户语料库表';
