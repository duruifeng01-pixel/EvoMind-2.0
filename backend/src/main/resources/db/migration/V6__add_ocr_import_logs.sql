-- V6__add_ocr_import_logs.sql
-- 添加OCR导入日志表，用于记录截图识别历史

CREATE TABLE IF NOT EXISTS ocr_import_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    platform VARCHAR(20) COMMENT '平台类型(xiaohongshu/weixin/douyin等)',
    image_hash VARCHAR(64) COMMENT '图片MD5哈希(用于去重)',
    original_image_url VARCHAR(512) COMMENT '原始图片URL',
    raw_result TEXT COMMENT 'OCR原始结果JSON',
    detected_count INT DEFAULT 0 COMMENT '检测到的博主数量',
    imported_count INT DEFAULT 0 COMMENT '实际导入数量',
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' COMMENT '状态: PROCESSING/SUCCESS/CONFIRMED/FAILED',
    error_message VARCHAR(512) COMMENT '错误信息',
    processing_time_ms BIGINT COMMENT '处理耗时(毫秒)',
    confirmed_at DATETIME COMMENT '确认时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_task_id (task_id),
    INDEX idx_user_id (user_id),
    INDEX idx_user_image_hash (user_id, image_hash),
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OCR导入日志表';
