-- 创建讨论主题表
CREATE TABLE IF NOT EXISTS discussions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_key VARCHAR(8) NOT NULL COMMENT '日期格式：20250214',
    title VARCHAR(200) NOT NULL COMMENT '讨论标题',
    content TEXT NOT NULL COMMENT '讨论内容',
    topic_tag VARCHAR(50) COMMENT '话题标签',
    related_source_ids VARCHAR(500) COMMENT '关联的信息源ID列表，逗号分隔',
    related_card_ids VARCHAR(500) COMMENT '关联的认知卡片ID列表，逗号分隔',
    ai_prompt TEXT COMMENT 'AI对话的Prompt模板',
    participant_count INT DEFAULT 0 COMMENT '参与人数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    is_published BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已发布',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    created_by BIGINT COMMENT '创建者ID，系统生成则为null',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date_key (date_key),
    KEY idx_is_published (is_published),
    KEY idx_date_key (date_key),
    KEY idx_topic_tag (topic_tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日讨论主题表';

-- 创建讨论评论表
CREATE TABLE IF NOT EXISTS discussion_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    discussion_id BIGINT NOT NULL COMMENT '讨论ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT COMMENT '回复的评论ID，一级评论为null',
    reply_to_user_id BIGINT COMMENT '回复给哪个用户ID',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    is_top BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除',
    deleted_at TIMESTAMP NULL COMMENT '删除时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_discussion_id (discussion_id),
    KEY idx_user_id (user_id),
    KEY idx_parent_id (parent_id),
    KEY idx_is_deleted (is_deleted),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='讨论评论表';

-- 插入示例今日讨论数据
INSERT INTO discussions (date_key, title, content, topic_tag, is_published, published_at, created_at, updated_at) 
VALUES (
    DATE_FORMAT(CURDATE(), '%Y%m%d'),
    '今日话题：AI会如何改变我们的工作方式？',
    '随着AI技术的快速发展，越来越多的工作开始被自动化取代。\n\n有人认为AI会创造更多新岗位，有人则担心大规模失业。\n\n你如何看待AI对就业市场的影响？欢迎在评论区分享你的观点。',
    'AI与未来',
    TRUE,
    NOW(),
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE updated_at = NOW();
