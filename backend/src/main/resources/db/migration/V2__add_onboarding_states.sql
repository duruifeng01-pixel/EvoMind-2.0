-- 创建新手引导状态表
CREATE TABLE IF NOT EXISTS onboarding_states (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_step INT NOT NULL DEFAULT 0,
    total_steps INT NOT NULL DEFAULT 5,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    trial_started_at TIMESTAMP NULL,
    trial_expired_at TIMESTAMP NULL,
    is_trial_active BOOLEAN NOT NULL DEFAULT FALSE,
    skipped_steps VARCHAR(100) NULL,
    last_activity_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_trial_active (is_trial_active),
    KEY idx_trial_expired_at (trial_expired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新手引导状态表';

-- 修改用户表添加是否完成新手引导标识
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS onboarding_completed_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS trial_activated BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS trial_activated_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS trial_expires_at TIMESTAMP NULL;

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_onboarding ON users(onboarding_completed);
CREATE INDEX IF NOT EXISTS idx_users_trial ON users(trial_activated, trial_expires_at);
