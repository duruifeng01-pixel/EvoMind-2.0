-- EvoMind 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS evomind CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE evomind;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    password VARCHAR(255) COMMENT '密码（加密）',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    wechat_openid VARCHAR(100) COMMENT '微信OpenID',
    wechat_unionid VARCHAR(100) COMMENT '微信UnionID',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, SUSPENDED, DELETED',
    last_login_at DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME COMMENT '软删除时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户订阅表
CREATE TABLE IF NOT EXISTS user_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    plan_type VARCHAR(50) NOT NULL COMMENT '套餐类型：BASIC, PREMIUM, CUSTOM',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, EXPIRED, CANCELLED',
    started_at DATETIME NOT NULL COMMENT '开始时间',
    expired_at DATETIME NOT NULL COMMENT '过期时间',
    auto_renew BOOLEAN DEFAULT TRUE COMMENT '自动续费',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订阅表';

-- 订阅套餐目录表
CREATE TABLE IF NOT EXISTS plan_catalogs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_type VARCHAR(50) NOT NULL COMMENT '套餐类型',
    plan_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格',
    period VARCHAR(20) NOT NULL COMMENT '周期：WEEKLY, MONTHLY, YEARLY',
    features JSON COMMENT '功能列表',
    description TEXT COMMENT '描述',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否上架',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐目录表';

-- 信息源表
CREATE TABLE IF NOT EXISTS sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    platform VARCHAR(50) NOT NULL COMMENT '平台：XIAOHONGSHU, ZHIHU, WECHAT, DOUYIN',
    author_name VARCHAR(100) COMMENT '博主名称',
    author_id VARCHAR(100) COMMENT '博主ID',
    author_avatar VARCHAR(255) COMMENT '博主头像',
    source_url VARCHAR(500) COMMENT '来源链接',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, PAUSED, DELETED',
    update_frequency VARCHAR(20) DEFAULT 'DAILY' COMMENT '更新频率',
    last_sync_at DATETIME COMMENT '最后同步时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信息源表';

-- 认知卡片表
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    source_id BIGINT COMMENT '信息源ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    one_sentence_summary VARCHAR(200) COMMENT '一句话导读',
    content TEXT COMMENT '核心内容（Markdown）',
    key_insights JSON COMMENT '核心观点',
    golden_sentences JSON COMMENT '金句列表',
    cases JSON COMMENT '案例列表',
    mind_map_json JSON COMMENT '脑图JSON',
    has_conflict BOOLEAN DEFAULT FALSE COMMENT '是否有认知冲突',
    conflict_card_ids VARCHAR(500) COMMENT '关联冲突卡片ID',
    source_title VARCHAR(200) COMMENT '原文标题',
    keywords VARCHAR(500) COMMENT '关键词',
    reading_time_minutes INT COMMENT '预估阅读时长（分钟）',
    generate_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '生成状态：PENDING, PROCESSING, COMPLETED, FAILED',
    is_favorite BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
    is_archived BOOLEAN DEFAULT FALSE COMMENT '是否归档',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (source_id) REFERENCES sources(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认知卡片表';

-- 脑图节点表
CREATE TABLE IF NOT EXISTS mindmap_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL COMMENT '卡片ID',
    node_id VARCHAR(64) NOT NULL COMMENT '节点唯一标识',
    parent_node_id VARCHAR(64) COMMENT '父节点ID',
    node_text TEXT NOT NULL COMMENT '节点文本',
    node_type VARCHAR(20) DEFAULT 'TOPIC' COMMENT '节点类型：ROOT, TOPIC, SUBTOPIC, DETAIL',
    level INT DEFAULT 0 COMMENT '层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    has_original_reference BOOLEAN DEFAULT FALSE COMMENT '是否关联原文',
    original_content_id BIGINT COMMENT '原文内容ID',
    original_paragraph_index INT COMMENT '原文段落索引',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    INDEX idx_card_node (card_id, node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脑图节点表';

-- 原文内容表
CREATE TABLE IF NOT EXISTS source_contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_id BIGINT NOT NULL COMMENT '信息源ID',
    original_url VARCHAR(500) COMMENT '原文链接',
    title VARCHAR(200) COMMENT '标题',
    content TEXT COMMENT '完整内容',
    paragraphs JSON COMMENT '分段内容（数组）',
    images JSON COMMENT '图片列表',
    published_at DATETIME COMMENT '发布时间',
    crawled_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '抓取时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (source_id) REFERENCES sources(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原文内容表';

-- 认知冲突表
CREATE TABLE IF NOT EXISTS card_conflicts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id_1 BIGINT NOT NULL COMMENT '卡片1ID',
    card_id_2 BIGINT NOT NULL COMMENT '卡片2ID',
    conflict_type VARCHAR(50) COMMENT '冲突类型：OPPOSITE_VIEW, DIFFERENT_FACT, CONTRADICTORY_DATA',
    conflict_score DECIMAL(5, 4) COMMENT '冲突分数（0-1）',
    conflict_description TEXT COMMENT '冲突描述',
    suggested_comparison TEXT COMMENT '建议对比方式',
    is_acknowledged BOOLEAN DEFAULT FALSE COMMENT '用户是否已确认',
    acknowledged_at DATETIME COMMENT '确认时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (card_id_1) REFERENCES cards(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id_2) REFERENCES cards(id) ON DELETE CASCADE,
    UNIQUE KEY uk_conflict (card_id_1, card_id_2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认知冲突表';

-- 每日一问表
CREATE TABLE IF NOT EXISTS daily_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question TEXT NOT NULL COMMENT '问题内容',
    context_card_ids JSON COMMENT '关联卡片ID',
    generated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    expires_at DATETIME COMMENT '过期时间',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING, ANSWERED, EXPIRED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日一问表';

-- 讨论记录表
CREATE TABLE IF NOT EXISTS discussions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    question_id BIGINT COMMENT '关联问题ID',
    message TEXT NOT NULL COMMENT '消息内容',
    role VARCHAR(20) NOT NULL COMMENT '角色：USER, AI',
    message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '类型：TEXT, SUMMARY',
    parent_id BIGINT COMMENT '父消息ID（用于追问链）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES daily_questions(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_id) REFERENCES discussions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='讨论记录表';

-- 挑战任务表
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    description TEXT COMMENT '任务描述',
    difficulty_level VARCHAR(20) DEFAULT 'BEGINNER' COMMENT '难度：BEGINNER, INTERMEDIATE, ADVANCED',
    task_type VARCHAR(50) COMMENT '任务类型：READING, WRITING, REFLECTION, PRACTICE',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING, IN_PROGRESS, COMPLETED, ABANDONED',
    started_at DATETIME COMMENT '开始时间',
    completed_at DATETIME COMMENT '完成时间',
    due_at DATETIME COMMENT '截止时间',
    artifact_url VARCHAR(500) COMMENT '作品链接',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挑战任务表';

-- 订阅订单表
CREATE TABLE IF NOT EXISTS subscription_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_no VARCHAR(64) UNIQUE NOT NULL COMMENT '订单号',
    plan_type VARCHAR(50) NOT NULL COMMENT '套餐类型',
    amount DECIMAL(10, 2) NOT NULL COMMENT '金额',
    cost_breakdown JSON COMMENT '成本明细',
    payment_method VARCHAR(20) COMMENT '支付方式：WECHAT, ALIPAY',
    payment_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING, PAID, FAILED, REFUNDED',
    paid_at DATETIME COMMENT '支付时间',
    transaction_id VARCHAR(100) COMMENT '第三方支付流水号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅订单表';

-- AI调用日志表
CREATE TABLE IF NOT EXISTS ai_call_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    api_type VARCHAR(50) NOT NULL COMMENT 'API类型',
    request_tokens INT DEFAULT 0 COMMENT '请求token数',
    response_tokens INT DEFAULT 0 COMMENT '响应token数',
    cost DECIMAL(10, 6) COMMENT '成本',
    request_params JSON COMMENT '请求参数',
    response_data JSON COMMENT '响应数据',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态：SUCCESS, FAILED',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用日志表';

-- 新手引导状态表
CREATE TABLE IF NOT EXISTS onboarding_states (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    current_step INT DEFAULT 0 COMMENT '当前步骤（0-5）',
    total_steps INT DEFAULT 5 COMMENT '总步骤数',
    is_completed BOOLEAN DEFAULT FALSE COMMENT '是否已完成',
    completed_at DATETIME COMMENT '完成时间',
    trial_started_at DATETIME COMMENT '试用期开始时间',
    trial_expired_at DATETIME COMMENT '试用期结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新手引导状态表';

-- 隐私数据导出记录表
CREATE TABLE IF NOT EXISTS privacy_exports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    export_type VARCHAR(50) NOT NULL COMMENT '导出类型：FULL, CARDS, SOURCES',
    status VARCHAR(20) DEFAULT 'PROCESSING' COMMENT '状态：PROCESSING, COMPLETED, FAILED',
    file_url VARCHAR(500) COMMENT '文件下载链接',
    file_size BIGINT COMMENT '文件大小（字节）',
    expired_at DATETIME COMMENT '链接过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME COMMENT '完成时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隐私数据导出记录表';

-- 创建索引
CREATE INDEX idx_cards_user_id ON cards(user_id);
CREATE INDEX idx_cards_source_id ON cards(source_id);
CREATE INDEX idx_cards_created_at ON cards(created_at);
CREATE INDEX idx_sources_user_id ON sources(user_id);
CREATE INDEX idx_discussions_user_id ON discussions(user_id);
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_orders_user_id ON subscription_orders(user_id);
CREATE INDEX idx_ai_logs_user_id ON ai_call_logs(user_id);
CREATE INDEX idx_ai_logs_created_at ON ai_call_logs(created_at);

-- 插入默认套餐数据
INSERT INTO plan_catalogs (plan_type, plan_name, price, period, features, description) VALUES
('BASIC', '基础版', 9.90, 'WEEKLY', '["信息源≤20", "每日一问", "基础脑图"]', '适合轻度用户，基础功能体验'),
('PREMIUM', '进阶版', 29.90, 'WEEKLY', '["信息源≤50", "无限每日一问", "高级脑图", "冲突标记", "Agent对话"]', '适合深度学习者，完整功能'),
('CUSTOM', '自由版', 0.00, 'WEEKLY', '["按需付费", "透明算力成本"]', '按实际使用量计费，成本透明');
