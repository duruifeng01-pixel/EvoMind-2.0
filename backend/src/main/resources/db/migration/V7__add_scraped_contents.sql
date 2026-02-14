-- V7__add_scraped_contents.sql
-- 添加抓取内容表，用于存储从各平台抓取的文章/视频内容

CREATE TABLE IF NOT EXISTS scraped_contents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    source_url VARCHAR(2048) NOT NULL COMMENT '原始链接URL',
    platform VARCHAR(20) COMMENT '平台类型(xiaohongshu/weixin/zhihu/douyin)',
    title VARCHAR(500) COMMENT '内容标题',
    author VARCHAR(100) COMMENT '作者名称',
    author_avatar VARCHAR(512) COMMENT '作者头像URL',
    content LONGTEXT COMMENT '正文内容(HTML或纯文本)',
    summary VARCHAR(2000) COMMENT '内容摘要',
    images_json TEXT COMMENT '图片列表JSON',
    publish_time DATETIME COMMENT '发布时间',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/SCRAPING/SUCCESS/FAILED/RETRYING',
    error_message VARCHAR(500) COMMENT '错误信息',
    processing_time_ms BIGINT COMMENT '处理耗时(毫秒)',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    content_hash VARCHAR(64) COMMENT '内容MD5哈希(去重)',
    word_count INT COMMENT '字数统计',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_task_id (task_id),
    INDEX idx_user_id (user_id),
    INDEX idx_source_url (source_url),
    INDEX idx_user_url (user_id, source_url),
    INDEX idx_content_hash (content_hash),
    INDEX idx_status (status),
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抓取内容表';
