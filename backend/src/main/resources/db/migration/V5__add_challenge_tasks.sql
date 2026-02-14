-- 创建挑战任务表
CREATE TABLE IF NOT EXISTS challenge_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_key VARCHAR(8) NOT NULL COMMENT '日期格式：20250214',
    title VARCHAR(100) NOT NULL COMMENT '任务标题',
    description TEXT NOT NULL COMMENT '任务描述',
    task_type VARCHAR(30) NOT NULL COMMENT 'READ_CARDS/阅读卡片、ADD_SOURCES/添加信息源、CREATE_NOTES/创建笔记、COMPLETE_DISCUSSION/参与讨论、SHARE_INSIGHT/分享洞见、DAILY_CHECKIN/每日签到',
    target_count INT NOT NULL COMMENT '目标完成次数',
    reward_points INT DEFAULT 10 COMMENT '完成奖励积分',
    reward_trial_days INT DEFAULT 0 COMMENT '额外奖励体验天数',
    is_published BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已发布',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date_key (date_key),
    KEY idx_is_published (is_published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='挑战任务表';

-- 创建用户任务进度表
CREATE TABLE IF NOT EXISTS user_task_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    date_key VARCHAR(8) NOT NULL COMMENT '日期格式：20250214',
    current_count INT DEFAULT 0 COMMENT '当前完成进度',
    is_completed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已完成',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    reward_claimed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '奖励是否已领取',
    reward_claimed_at TIMESTAMP NULL COMMENT '奖励领取时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_task_date (user_id, task_id, date_key),
    KEY idx_user_date (user_id, date_key),
    KEY idx_user_completed (user_id, is_completed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户任务进度表';

-- 插入今日示例任务
INSERT INTO challenge_tasks (date_key, title, description, task_type, target_count, reward_points, is_published, published_at) 
VALUES (
    DATE_FORMAT(CURDATE(), '%Y%m%d'),
    '今日挑战：阅读3张认知卡片',
    '阅读是认知升级的基础。今天挑战阅读3张认知卡片，深入理解其中的核心观点。',
    'READ_CARDS',
    3,
    10,
    TRUE,
    NOW()
) ON DUPLICATE KEY UPDATE updated_at = NOW();
