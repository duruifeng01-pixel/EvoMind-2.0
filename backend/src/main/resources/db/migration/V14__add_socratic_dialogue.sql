-- V14: 添加苏格拉底式对话系统表
-- 创建时间: 2026-02-15
-- 功能: AI苏格拉底式对话支持

-- 苏格拉底式对话会话表
CREATE TABLE socratic_dialogues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    discussion_id BIGINT NOT NULL COMMENT '关联的讨论主题ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '对话状态',
    current_round INT DEFAULT 0 COMMENT '当前对话轮次',
    max_rounds INT DEFAULT 5 COMMENT '最大对话轮次',
    initial_question TEXT COMMENT '初始问题',
    final_insight TEXT COMMENT '最终洞察总结',
    insight_generated_at DATETIME COMMENT '洞察生成时间',
    user_satisfaction INT COMMENT '用户满意度评分 1-5',
    is_abandoned BOOLEAN DEFAULT FALSE COMMENT '是否被放弃',
    abandoned_at DATETIME COMMENT '放弃时间',
    completed_at DATETIME COMMENT '完成时间',
    last_message_at DATETIME COMMENT '最后消息时间',
    total_messages INT DEFAULT 0 COMMENT '总消息数',
    version BIGINT DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (discussion_id) REFERENCES discussions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_dialogue_user_status (user_id, status),
    INDEX idx_dialogue_discussion (discussion_id),
    INDEX idx_dialogue_status_time (status, last_message_at),
    INDEX idx_dialogue_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='苏格拉底式对话会话表';

-- 苏格拉底式对话消息表
CREATE TABLE socratic_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dialogue_id BIGINT NOT NULL COMMENT '关联的对话会话ID',
    round INT NOT NULL COMMENT '对话轮次',
    role VARCHAR(20) NOT NULL COMMENT '消息角色: AI/USER',
    content TEXT NOT NULL COMMENT '消息内容',
    type VARCHAR(30) NOT NULL COMMENT '消息类型',
    question_category VARCHAR(50) COMMENT '问题类别',
    depth_level INT DEFAULT 1 COMMENT '追问深度层级 1-5',
    is_follow_up BOOLEAN DEFAULT FALSE COMMENT '是否是追问',
    follow_up_target_id BIGINT COMMENT '追问的目标消息ID',
    ai_analysis TEXT COMMENT 'AI对用户回答的分析',
    thinking_hints TEXT COMMENT '思考提示',
    key_points_extracted TEXT COMMENT '提取的关键点',
    user_intent VARCHAR(100) COMMENT '用户意图分析',
    sentiment_score DECIMAL(3,2) COMMENT '情感分数 -1.0到1.0',
    response_time_seconds INT COMMENT 'AI响应时间（秒）',
    token_count INT COMMENT 'Token使用量',
    sequence_number INT NOT NULL COMMENT '消息序号',
    is_final_summary BOOLEAN DEFAULT FALSE COMMENT '是否是最终总结',
    branch_from_message_id BIGINT COMMENT '分支来源消息ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (dialogue_id) REFERENCES socratic_dialogues(id) ON DELETE CASCADE,
    INDEX idx_message_dialogue_seq (dialogue_id, sequence_number),
    INDEX idx_message_dialogue_round (dialogue_id, round),
    INDEX idx_message_type (type),
    INDEX idx_message_role (role),
    INDEX idx_message_depth (depth_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='苏格拉底式对话消息表';

-- 添加讨论表的AI Prompt字段（如果还不存在）
SET @dbname = DATABASE();
SET @tablename = 'discussions';
SET @columnname = 'ai_prompt';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @dbname
        AND TABLE_NAME = @tablename
        AND COLUMN_NAME = @columnname
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ai_prompt TEXT COMMENT "AI对话的Prompt模板"')
));
PREPARE addColumnIfNotExists FROM @preparedStatement;
EXECUTE addColumnIfNotExists;
DEALLOCATE PREPARE addColumnIfNotExists;
