package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 敏感词库实体
 * 支持敏感词的增删改查和分类管理
 */
@Data
@Entity
@Table(name = "sensitive_words", indexes = {
    @Index(name = "idx_sensitive_word_word", columnList = "word"),
    @Index(name = "idx_sensitive_word_category", columnList = "category"),
    @Index(name = "idx_sensitive_word_enabled", columnList = "enabled")
})
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensitiveWord extends BaseEntity {

    /**
     * 敏感词内容
     */
    @Column(name = "word", nullable = false, length = 200)
    private String word;

    /**
     * 敏感词分类
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private WordCategory category;

    /**
     * 敏感级别
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private SensitiveLevel level;

    /**
     * 匹配模式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "match_mode", nullable = false, length = 20)
    private MatchMode matchMode;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 描述说明
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 来源（系统预设/用户添加/外部导入）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20)
    private WordSource source;

    /**
     * 命中次数统计
     */
    @Column(name = "hit_count")
    private Long hitCount = 0L;

    /**
     * 最后命中时间
     */
    @Column(name = "last_hit_at")
    private LocalDateTime lastHitAt;

    /**
     * 生效时间（为空则立即生效）
     */
    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    /**
     * 失效时间（为空则永不过期）
     */
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    /**
     * 创建人ID（0表示系统预设）
     */
    @Column(name = "created_by")
    private Long createdBy = 0L;

    /**
     * 敏感词分类枚举
     */
    public enum WordCategory {
        POLITICS("政治敏感", "涉及政治人物、事件、敏感话题"),
        PORNOGRAPHY("色情", "色情、低俗、淫秽内容"),
        VIOLENCE("暴力", "暴力、血腥、犯罪相关内容"),
        TERRORISM("恐怖主义", "恐怖组织、极端主义"),
        GAMBLING("赌博", "赌博、博彩相关内容"),
        FRAUD("诈骗", "诈骗、虚假信息"),
        ABUSE("辱骂", "侮辱、攻击性语言"),
        ADVERTISEMENT("广告", "违规广告、垃圾信息"),
        PRIVACY("隐私", "个人隐私信息"),
        CUSTOM("自定义", "用户自定义敏感词");

        private final String label;
        private final String description;

        WordCategory(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 敏感级别枚举
     */
    public enum SensitiveLevel {
        LOW(1, "低", "提示性检测"),
        MEDIUM(2, "中", "需要关注"),
        HIGH(3, "高", "严格拦截"),
        CRITICAL(4, "极高", "立即阻断");

        private final int level;
        private final String label;
        private final String description;

        SensitiveLevel(int level, String label, String description) {
            this.level = level;
            this.label = label;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 匹配模式枚举
     */
    public enum MatchMode {
        EXACT("精确匹配", "完全匹配整个词"),
        CONTAINS("包含匹配", "文本中包含该词"),
        PREFIX("前缀匹配", "以该词开头"),
        SUFFIX("后缀匹配", "以该词结尾"),
        REGEX("正则匹配", "使用正则表达式匹配"),
        FUZZY("模糊匹配", "相似度匹配");

        private final String label;
        private final String description;

        MatchMode(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 来源枚举
     */
    public enum WordSource {
        SYSTEM,    // 系统预设
        IMPORTED,  // 外部导入
        MANUAL     // 手动添加
    }

    /**
     * 增加命中次数
     */
    public void incrementHitCount() {
        this.hitCount++;
        this.lastHitAt = LocalDateTime.now();
    }

    /**
     * 检查是否在有效期内
     */
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        if (effectiveFrom != null && now.isBefore(effectiveFrom)) {
            return false;
        }
        if (effectiveTo != null && now.isAfter(effectiveTo)) {
            return false;
        }
        return enabled;
    }

    /**
     * 是否需要阻断（极高敏感级别）
     */
    public boolean shouldBlock() {
        return level == SensitiveLevel.CRITICAL && isEffective();
    }

    /**
     * 是否需要人工复核（高敏感级别）
     */
    public boolean needManualReview() {
        return level == SensitiveLevel.HIGH && isEffective();
    }
}