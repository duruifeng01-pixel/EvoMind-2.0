-- V17: 添加AIGC内容合规相关表
-- 内容审核日志表 + 敏感词库表

-- ==================== 内容审核日志表 ====================
CREATE TABLE IF NOT EXISTS content_moderation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content_type VARCHAR(50) NOT NULL COMMENT '内容类型：CARD_AI_SUMMARY/CARD_AI_INSIGHT/SOCRATIC_DIALOGUE等',
    content_id VARCHAR(100) COMMENT '内容ID（关联的具体内容ID）',
    original_content TEXT COMMENT '原始内容（脱敏存储）',
    content_summary VARCHAR(500) COMMENT '内容摘要（用于日志展示）',
    moderation_status VARCHAR(20) NOT NULL COMMENT '审核状态：PENDING/PROCESSING/APPROVED/REJECTED/NEED_REVIEW/ERROR',
    moderation_type VARCHAR(20) NOT NULL COMMENT '审核方式：AUTO_SENSITIVE_WORD/AUTO_BAIDU_API/HYBRID/MANUAL_REVIEW',
    violation_type VARCHAR(50) COMMENT '违规类型：POLITICS/PORNOGRAPHY/VIOLENCE等',
    violation_details VARCHAR(1000) COMMENT '违规详情说明',
    hit_sensitive_words TEXT COMMENT '敏感词命中列表（JSON格式）',
    provider VARCHAR(50) COMMENT '审核服务商（百度/阿里/本地）',
    provider_response TEXT COMMENT '第三方API返回的原始结果（JSON）',
    request_id VARCHAR(100) COMMENT '审核请求ID（用于追溯）',
    is_ai_generated TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否AI生成内容',
    ai_model VARCHAR(100) COMMENT 'AI模型名称（如果是AI生成）',
    process_time_ms BIGINT COMMENT '审核耗时（毫秒）',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    manual_review_required TINYINT(1) DEFAULT 0 COMMENT '人工复核标记',
    manual_review_result VARCHAR(20) COMMENT '人工复核结果',
    reviewer_id BIGINT COMMENT '复核人ID',
    reviewed_at DATETIME COMMENT '复核时间',
    review_remark VARCHAR(500) COMMENT '复核备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_moderation_user_id (user_id),
    INDEX idx_moderation_content (content_id, content_type),
    INDEX idx_moderation_status (moderation_status),
    INDEX idx_moderation_created_at (created_at),
    INDEX idx_moderation_ai_generated (is_ai_generated),
    INDEX idx_moderation_manual_review (manual_review_required, moderation_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容审核日志表';

-- ==================== 敏感词库表 ====================
CREATE TABLE IF NOT EXISTS sensitive_words (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(200) NOT NULL COMMENT '敏感词内容',
    category VARCHAR(50) NOT NULL COMMENT '分类：POLITICS/PORNOGRAPHY/VIOLENCE/TERRORISM/GAMBLING/FRAUD/ABUSE/ADVERTISEMENT/PRIVACY/CUSTOM',
    level VARCHAR(20) NOT NULL COMMENT '敏感级别：LOW/MEDIUM/HIGH/CRITICAL',
    match_mode VARCHAR(20) NOT NULL DEFAULT 'CONTAINS' COMMENT '匹配模式：EXACT/CONTAINS/PREFIX/SUFFIX/REGEX/FUZZY',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    description VARCHAR(500) COMMENT '描述说明',
    source VARCHAR(20) COMMENT '来源：SYSTEM/IMPORTED/MANUAL',
    hit_count BIGINT DEFAULT 0 COMMENT '命中次数统计',
    last_hit_at DATETIME COMMENT '最后命中时间',
    effective_from DATETIME COMMENT '生效时间',
    effective_to DATETIME COMMENT '失效时间',
    created_by BIGINT DEFAULT 0 COMMENT '创建人ID（0表示系统预设）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_sensitive_word_word (word),
    INDEX idx_sensitive_word_category (category),
    INDEX idx_sensitive_word_enabled (enabled),
    INDEX idx_sensitive_word_level (level),
    INDEX idx_sensitive_word_hit_count (hit_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词库表';

-- ==================== 初始化系统预设敏感词 ====================

-- 政治敏感词（极高敏感级别）
INSERT INTO sensitive_words (word, category, level, match_mode, description, source, created_by, enabled) VALUES
('反动', 'POLITICS', 'CRITICAL', 'CONTAINS', '政治敏感词-系统自动添加', 'SYSTEM', 0, 1),
('颠覆', 'POLITICS', 'CRITICAL', 'CONTAINS', '政治敏感词-系统自动添加', 'SYSTEM', 0, 1),
('暴乱', 'POLITICS', 'CRITICAL', 'CONTAINS', '政治敏感词-系统自动添加', 'SYSTEM', 0, 1),
('分裂', 'POLITICS', 'CRITICAL', 'CONTAINS', '政治敏感词-系统自动添加', 'SYSTEM', 0, 1),
('台独', 'POLITICS', 'CRITICAL', 'CONTAINS', '政治敏感词-系统自动添加', 'SYSTEM', 0, 1),
('港独', 'POLITICS', 'CRITICAL', 'CONTAINS', '政治敏感词-系统自动添加', 'SYSTEM', 0, 1),
('藏独', 'POLITICS', 'CRITICAL', 'CONTAINS', '政治敏感词-系统自动添加', 'SYSTEM', 0, 1),
('疆独', 'POLITICS', 'CRITICAL', 'CONTAINS', '政治敏感词-系统自动添加', 'SYSTEM', 0, 1);

-- 色情词（高敏感级别）
INSERT INTO sensitive_words (word, category, level, match_mode, description, source, created_by, enabled) VALUES
('色情', 'PORNOGRAPHY', 'HIGH', 'CONTAINS', '色情词-系统自动添加', 'SYSTEM', 0, 1),
('淫秽', 'PORNOGRAPHY', 'HIGH', 'CONTAINS', '色情词-系统自动添加', 'SYSTEM', 0, 1),
('嫖娼', 'PORNOGRAPHY', 'HIGH', 'CONTAINS', '色情词-系统自动添加', 'SYSTEM', 0, 1),
('卖淫', 'PORNOGRAPHY', 'HIGH', 'CONTAINS', '色情词-系统自动添加', 'SYSTEM', 0, 1),
('裸聊', 'PORNOGRAPHY', 'HIGH', 'CONTAINS', '色情词-系统自动添加', 'SYSTEM', 0, 1),
('裸照', 'PORNOGRAPHY', 'HIGH', 'CONTAINS', '色情词-系统自动添加', 'SYSTEM', 0, 1);

-- 暴力词（高敏感级别）
INSERT INTO sensitive_words (word, category, level, match_mode, description, source, created_by, enabled) VALUES
('杀人', 'VIOLENCE', 'HIGH', 'CONTAINS', '暴力词-系统自动添加', 'SYSTEM', 0, 1),
('爆炸', 'VIOLENCE', 'HIGH', 'CONTAINS', '暴力词-系统自动添加', 'SYSTEM', 0, 1),
('恐怖袭击', 'VIOLENCE', 'HIGH', 'CONTAINS', '暴力词-系统自动添加', 'SYSTEM', 0, 1),
('暴力', 'VIOLENCE', 'MEDIUM', 'CONTAINS', '暴力词-系统自动添加', 'SYSTEM', 0, 1),
('血腥', 'VIOLENCE', 'MEDIUM', 'CONTAINS', '暴力词-系统自动添加', 'SYSTEM', 0, 1);

-- 诈骗词（高敏感级别）
INSERT INTO sensitive_words (word, category, level, match_mode, description, source, created_by, enabled) VALUES
('诈骗', 'FRAUD', 'HIGH', 'CONTAINS', '诈骗词-系统自动添加', 'SYSTEM', 0, 1),
('传销', 'FRAUD', 'HIGH', 'CONTAINS', '诈骗词-系统自动添加', 'SYSTEM', 0, 1),
('非法集资', 'FRAUD', 'HIGH', 'CONTAINS', '诈骗词-系统自动添加', 'SYSTEM', 0, 1),
('洗钱', 'FRAUD', 'HIGH', 'CONTAINS', '诈骗词-系统自动添加', 'SYSTEM', 0, 1),
('套现', 'FRAUD', 'MEDIUM', 'CONTAINS', '诈骗词-系统自动添加', 'SYSTEM', 0, 1);

-- 赌博词（高敏感级别）
INSERT INTO sensitive_words (word, category, level, match_mode, description, source, created_by, enabled) VALUES
('赌博', 'GAMBLING', 'HIGH', 'CONTAINS', '赌博词-系统自动添加', 'SYSTEM', 0, 1),
('博彩', 'GAMBLING', 'HIGH', 'CONTAINS', '赌博词-系统自动添加', 'SYSTEM', 0, 1),
('赌球', 'GAMBLING', 'HIGH', 'CONTAINS', '赌博词-系统自动添加', 'SYSTEM', 0, 1),
('六合彩', 'GAMBLING', 'HIGH', 'CONTAINS', '赌博词-系统自动添加', 'SYSTEM', 0, 1),
('赌场', 'GAMBLING', 'HIGH', 'CONTAINS', '赌博词-系统自动添加', 'SYSTEM', 0, 1);

-- 辱骂词（中敏感级别）
INSERT INTO sensitive_words (word, category, level, match_mode, description, source, created_by, enabled) VALUES
('傻逼', 'ABUSE', 'MEDIUM', 'CONTAINS', '辱骂词-系统自动添加', 'SYSTEM', 0, 1),
('蠢货', 'ABUSE', 'MEDIUM', 'CONTAINS', '辱骂词-系统自动添加', 'SYSTEM', 0, 1),
('去死', 'ABUSE', 'MEDIUM', 'CONTAINS', '辱骂词-系统自动添加', 'SYSTEM', 0, 1);

-- ==================== 添加配置项 ====================
INSERT INTO app_config (config_key, config_value, description, created_at, updated_at) VALUES
('moderation.baidu.enabled', 'false', '是否启用百度内容审核API', NOW(), NOW()),
('moderation.baidu.app_id', '', '百度内容审核APP ID', NOW(), NOW()),
('moderation.baidu.api_key', '', '百度内容审核API Key', NOW(), NOW()),
('moderation.baidu.secret_key', '', '百度内容审核Secret Key', NOW(), NOW()),
('moderation.sensitive_word.strict_mode', 'false', '敏感词严格模式（true=所有级别都阻断，false=仅CRITICAL阻断）', NOW(), NOW()),
('moderation.ai_content.auto_mark', 'true', 'AI生成内容自动添加标注', NOW(), NOW()),
('moderation.log.retention_days', '90', '审核日志保留天数', NOW(), NOW());

-- 避免重复插入（如果配置已存在则忽略）
-- 注意：实际执行时如果配置已存在会有主键冲突，这是预期的行为