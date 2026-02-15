package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 用户语料库实体
 * 
 * 存储用户生成的知识资产，包括：
 * - 苏格拉底式对话洞察
 * - 用户笔记/随想
 * - 收藏高亮/标注
 * - AI辅助总结
 * 
 * 与 Card 的区别：
 * - Card：来自外部信息源的认知卡片，参与 Feed 流推荐（7:3 混合流）
 * - UserCorpus：用户自己生成的知识资产，不参与 Feed 流推荐
 */
@Entity
@Table(name = "user_corpus")
@Getter
@Setter
public class UserCorpus extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "content_text", columnDefinition = "longtext")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String contentText;

    @Column(name = "summary_text", length = 1000)
    private String summaryText;

    @Column(name = "one_sentence_summary", length = 200)
    private String oneSentenceSummary;

    /**
     * 语料类型
     */
    @Column(name = "corpus_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private CorpusType corpusType = CorpusType.SOCRATIC_INSIGHT;

    /**
     * 关联来源类型
     */
    @Column(name = "source_type", length = 30)
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    /**
     * 关联来源ID（如对话ID、笔记ID等）
     */
    @Column(name = "source_id")
    private Long sourceId;

    /**
     * 关联来源URL/标识
     */
    @Column(name = "source_ref", length = 512)
    private String sourceRef;

    /**
     * 关联的讨论ID（如果是从讨论生成的洞察）
     */
    @Column(name = "discussion_id")
    private Long discussionId;

    /**
     * 关键词标签，逗号分隔
     */
    @Column(name = "keywords", length = 500)
    private String keywords;

    /**
     * 阅读时长（分钟）
     */
    @Column(name = "reading_time_minutes")
    private Integer readingTimeMinutes;

    /**
     * 是否收藏
     */
    @Column(name = "is_favorite")
    private Boolean isFavorite = false;

    /**
     * 是否置顶
     */
    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    /**
     * 置顶时间
     */
    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    /**
     * 归档状态
     */
    @Column(name = "is_archived")
    private Boolean isArchived = false;

    /**
     * 归档时间
     */
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    /**
     * 查看次数
     */
    @Column(name = "view_count")
    private Integer viewCount = 0;

    /**
     * 最后查看时间
     */
    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    /**
     * 关联的原始卡片ID（如果是从卡片提取的洞察）
     */
    @Column(name = "related_card_id")
    private Long relatedCardId;

    /**
     * 语料内容类型枚举
     */
    public enum CorpusType {
        SOCRATIC_INSIGHT("苏格拉底式对话洞察", "通过苏格拉底式对话生成的深度思考洞察"),
        USER_NOTE("用户笔记", "用户自己记录的笔记和随想"),
        HIGHLIGHT("收藏高亮", "从外部内容中提取的高亮和标注"),
        AI_SUMMARY("AI总结", "AI辅助生成的内容总结"),
        REFLECTION("反思记录", "用户对某主题的深度反思"),
        INSIGHT("认知洞察", "其他类型的认知洞察");

        private final String displayName;
        private final String description;

        CorpusType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 来源类型枚举
     */
    public enum SourceType {
        SOCRATIC_DIALOGUE,
        DISCUSSION,
        CARD,
        MANUAL_INPUT,
        VOICE_NOTE,
        OCR_IMPORT,
        LINK_SCRAPE
    }

    /**
     * 增加查看次数
     */
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
        this.lastViewedAt = LocalDateTime.now();
    }

    /**
     * 置顶
     */
    public void pin() {
        this.isPinned = true;
        this.pinnedAt = LocalDateTime.now();
    }

    /**
     * 取消置顶
     */
    public void unpin() {
        this.isPinned = false;
        this.pinnedAt = null;
    }

    /**
     * 归档
     */
    public void archive() {
        this.isArchived = true;
        this.archivedAt = LocalDateTime.now();
    }

    /**
     * 取消归档
     */
    public void unarchive() {
        this.isArchived = false;
        this.archivedAt = null;
    }

    /**
     * 检查是否来自苏格拉底式对话
     */
    public boolean isFromSocraticDialogue() {
        return CorpusType.SOCRATIC_INSIGHT.equals(this.corpusType) &&
               SourceType.SOCRATIC_DIALOGUE.equals(this.sourceType);
    }

    /**
     * 获取类型显示名称
     */
    public String getTypeDisplayName() {
        return this.corpusType != null ? this.corpusType.getDisplayName() : "未知类型";
    }
}
