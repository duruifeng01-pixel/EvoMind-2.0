-- V15: 添加算力成本统计相关表
-- 创建时间: 2026-02-15
-- 功能: 支持算力成本记录和透明定价

-- 算力成本记录表
CREATE TABLE IF NOT EXISTS computing_cost_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',

    -- 成本指标
    source_count INT NOT NULL DEFAULT 0 COMMENT '信息源数量',
    conflict_mark_count INT NOT NULL DEFAULT 0 COMMENT '冲突标记次数',
    ocr_request_count INT NOT NULL DEFAULT 0 COMMENT 'OCR请求次数',
    ai_token_count BIGINT NOT NULL DEFAULT 0 COMMENT 'AI Token消耗数',
    dialogue_turn_count INT NOT NULL DEFAULT 0 COMMENT '对话轮数',
    model_training_count INT NOT NULL DEFAULT 0 COMMENT '模型训练次数',
    feed_card_count INT NOT NULL DEFAULT 0 COMMENT '信息流卡片数',
    crawl_request_count INT NOT NULL DEFAULT 0 COMMENT '抓取请求次数',

    -- 成本金额（元）
    ocr_cost DECIMAL(10, 4) NOT NULL DEFAULT 0.0000 COMMENT 'OCR成本',
    ai_cost DECIMAL(10, 4) NOT NULL DEFAULT 0.0000 COMMENT 'AI成本',
    crawl_cost DECIMAL(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '抓取成本',
    storage_cost DECIMAL(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '存储成本',
    total_cost DECIMAL(10, 4) NOT NULL DEFAULT 0.0000 COMMENT '总成本',

    -- 订阅费用计算
    subscription_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订阅费用',
    cost_multiplier INT NOT NULL DEFAULT 2 COMMENT '成本倍数（默认2倍）',
    calculated_at DATETIME COMMENT '计算时间',

    -- 审计字段
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    -- 约束
    UNIQUE KEY uk_user_date (user_id, record_date),
    KEY idx_user_id (user_id),
    KEY idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='算力成本记录表';

-- 成本单价配置表
CREATE TABLE IF NOT EXISTS cost_unit_prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    price_code VARCHAR(50) NOT NULL COMMENT '价格代码',
    price_name VARCHAR(100) NOT NULL COMMENT '价格名称',
    price_description VARCHAR(500) COMMENT '价格描述',
    unit_price DECIMAL(10, 6) NOT NULL COMMENT '单价',
    unit_type VARCHAR(50) NOT NULL COMMENT '单位类型：per_request, per_token, per_mb, per_day',
    service_category VARCHAR(50) NOT NULL COMMENT '服务类别：ocr, ai, crawl, storage',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否生效',
    effective_date DATETIME NOT NULL COMMENT '生效时间',
    expiry_date DATETIME COMMENT '过期时间',

    -- 审计字段
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 约束
    UNIQUE KEY uk_price_code (price_code),
    KEY idx_service_category (service_category),
    KEY idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成本单价配置表';

-- 插入默认成本单价配置
INSERT INTO cost_unit_prices (price_code, price_name, price_description, unit_price, unit_type, service_category, is_active, effective_date) VALUES
('OCR_PER_REQUEST', 'OCR识别单价', '每次OCR文字识别服务费用', 0.050000, 'per_request', 'ocr', 1, '2026-01-01 00:00:00'),
('AI_PER_1K_TOKEN', 'AI调用单价', '每1000个token的AI调用费用', 0.015000, 'per_token', 'ai', 1, '2026-01-01 00:00:00'),
('AI_DIALOGUE_PER_TURN', '对话轮次单价', '每轮苏格拉底对话费用', 0.100000, 'per_request', 'ai', 1, '2026-01-01 00:00:00'),
('CRAWL_PER_REQUEST', '内容抓取单价', '每次内容抓取服务费用', 0.020000, 'per_request', 'crawl', 1, '2026-01-01 00:00:00'),
('STORAGE_PER_MB_PER_DAY', '存储单价', '每MB存储每天费用', 0.001000, 'per_mb', 'storage', 1, '2026-01-01 00:00:00'),
('SOURCE_BASE_PER_DAY', '信息源基础费', '每个信息源每天基础费用', 0.010000, 'per_day', 'crawl', 1, '2026-01-01 00:00:00'),
('CONFLICT_MARK_PER_ITEM', '冲突标记单价', '每次冲突标记处理费用', 0.005000, 'per_request', 'ai', 1, '2026-01-01 00:00:00'),
('TRAINING_PER_SESSION', '模型训练单价', '每次个性化模型训练费用', 0.500000, 'per_request', 'ai', 1, '2026-01-01 00:00:00');
