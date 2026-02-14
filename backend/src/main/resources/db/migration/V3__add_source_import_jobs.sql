-- 创建信息源导入任务表
CREATE TABLE IF NOT EXISTS source_import_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_type VARCHAR(20) NOT NULL COMMENT 'OCR_SCREENSHOT/OCR截图识别、LINK_SCRAPE/链接抓取',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/待处理、PROCESSING/处理中、COMPLETED/已完成、FAILED/失败、RETRYING/重试中',
    source_url VARCHAR(2048) NULL COMMENT '源链接URL',
    image_url VARCHAR(512) NULL COMMENT '图片URL或Base64引用',
    platform VARCHAR(50) NULL COMMENT '平台类型：xiaohongshu/weixin/zhihu/douyin',
    detected_authors TEXT NULL COMMENT '检测到的作者列表JSON',
    selected_authors TEXT NULL COMMENT '用户选中的作者列表JSON',
    error_message VARCHAR(500) NULL COMMENT '错误信息',
    started_at TIMESTAMP NULL COMMENT '开始处理时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信息源导入任务表';
