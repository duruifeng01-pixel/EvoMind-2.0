-- V14: 添加密码相关字段
-- 用于支持密码重置功能

-- 确保 users 表的 password 字段不为空
ALTER TABLE users 
MODIFY COLUMN password VARCHAR(255) NOT NULL COMMENT '加密密码';

-- 添加密码更新时间字段（用于安全审计）
ALTER TABLE users 
ADD COLUMN password_updated_at DATETIME NULL COMMENT '密码最后更新时间' AFTER password;

-- 添加密码重置令牌表（可选，用于邮件重置场景）
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token VARCHAR(255) NOT NULL COMMENT '重置令牌',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    used TINYINT(1) DEFAULT 0 COMMENT '是否已使用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_token (token),
    INDEX idx_expires_at (expires_at),
    
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密码重置令牌表';

-- 更新现有用户的密码更新时间（使用注册时间作为默认）
UPDATE users 
SET password_updated_at = created_at 
WHERE password_updated_at IS NULL;
