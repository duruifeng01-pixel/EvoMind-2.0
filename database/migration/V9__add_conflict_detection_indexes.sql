-- V9: 添加冲突检测相关索引和字段优化
-- 为提升冲突检测性能添加索引

-- 卡片冲突表索引优化
CREATE INDEX IF NOT EXISTS idx_card_conflicts_user_id ON card_conflicts(user_id);
CREATE INDEX IF NOT EXISTS idx_card_conflicts_card_id_1 ON card_conflicts(card_id_1);
CREATE INDEX IF NOT EXISTS idx_card_conflicts_card_id_2 ON card_conflicts(card_id_2);
CREATE INDEX IF NOT EXISTS idx_card_conflicts_acknowledged ON card_conflicts(user_id, is_acknowledged);
CREATE INDEX IF NOT EXISTS idx_card_conflicts_conflict_score ON card_conflicts(conflict_score);

-- 卡片表索引（用于冲突检测时的快速查询）
CREATE INDEX IF NOT EXISTS idx_cards_user_id_created ON cards(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_cards_has_conflict ON cards(has_conflict) WHERE has_conflict = TRUE;

-- 添加注释说明
COMMENT ON TABLE card_conflicts IS '卡片观点冲突记录表';
COMMENT ON COLUMN card_conflicts.conflict_type IS '冲突类型: CONTRADICTORY-观点对立, COMPLEMENTARY-观点互补, DIFFERENT_PERSPECTIVE-不同视角';
COMMENT ON COLUMN card_conflicts.conflict_score IS '冲突分数 0-1，越高表示冲突越明显';
COMMENT ON COLUMN card_conflicts.similarity_score IS '相似度分数 0-1，表示两张卡片主题相关程度';
