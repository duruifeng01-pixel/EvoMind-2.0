-- V12: 添加内容采集调度相关表
-- 用于实现7:3信息流的自动内容采集

-- 内容采集任务表
CREATE TABLE IF NOT EXISTS content_crawl_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    platform VARCHAR(50),
    source_url VARCHAR(512),
    job_type VARCHAR(20) DEFAULT 'SCHEDULED',
    status VARCHAR(20) DEFAULT 'PENDING',
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    articles_found INT DEFAULT 0,
    articles_new INT DEFAULT 0,
    articles_duplicated INT DEFAULT 0,
    error_message VARCHAR(1000),
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_source_id (source_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 采集内容表
CREATE TABLE IF NOT EXISTS crawled_contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    platform VARCHAR(50),
    original_url VARCHAR(512) NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    title VARCHAR(200),
    content TEXT,
    author VARCHAR(100),
    published_at TIMESTAMP NULL,
    crawled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'RAW',
    is_duplicate BOOLEAN DEFAULT FALSE,
    duplicate_of_id BIGINT,
    quality_score INT DEFAULT 0,
    category VARCHAR(50),
    tags VARCHAR(200),
    card_id BIGINT,
    is_system_discovered BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_content_hash (content_hash),
    INDEX idx_source_id (source_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_crawled_at (crawled_at),
    INDEX idx_is_system_discovered (is_system_discovered),
    INDEX idx_quality_score (quality_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 添加注释
ALTER TABLE content_crawl_jobs COMMENT = '内容采集任务表，记录每次采集任务的执行状态';
ALTER TABLE crawled_contents COMMENT = '采集内容表，存储从信息源抓取到的原始内容';
