-- 语音笔记表
-- 存储用户语音记录的元数据和转写文本

CREATE TABLE IF NOT EXISTS voice_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    audio_url VARCHAR(512) COMMENT '音频文件URL',
    audio_duration_seconds INT COMMENT '音频时长（秒）',
    audio_format VARCHAR(10) DEFAULT 'mp3' COMMENT '音频格式(mp3/wav/pcm)',
    file_size_bytes BIGINT COMMENT '文件大小（字节）',
    transcribed_text VARCHAR(4000) COMMENT '转写后的文本内容',
    transcribe_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '转写状态(PENDING/PROCESSING/SUCCESS/FAILED)',
    transcribe_error VARCHAR(500) COMMENT '转写错误信息',
    title VARCHAR(200) COMMENT '笔记标题',
    tags VARCHAR(500) COMMENT '标签，逗号分隔',
    is_favorite BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
    is_archived BOOLEAN DEFAULT FALSE COMMENT '是否归档',
    recorded_at TIMESTAMP COMMENT '录音时间',
    transcribed_at TIMESTAMP COMMENT '转写完成时间',
    local_path VARCHAR(512) COMMENT '本地存储路径',
    is_synced BOOLEAN DEFAULT FALSE COMMENT '是否已同步',
    synced_at TIMESTAMP COMMENT '同步时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_user_recorded_at (user_id, recorded_at),
    INDEX idx_user_favorite (user_id, is_favorite),
    INDEX idx_user_archived (user_id, is_archived),
    INDEX idx_transcribe_status (transcribe_status),
    FULLTEXT INDEX ft_transcribed_text (transcribed_text, title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='语音笔记表';
