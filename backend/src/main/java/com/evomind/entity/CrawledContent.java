package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 采集到的内容实体
 * 存储从信息源抓取到的原始内容
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "crawled_contents")
public class CrawledContent extends BaseEntity {

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "platform", length = 50)
    private String platform;

    @Column(name = "original_url", length = 512, nullable = false)
    private String originalUrl;

    @Column(name = "content_hash", length = 64, nullable = false)
    private String contentHash;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "author", length = 100)
    private String author;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "crawled_at")
    private LocalDateTime crawledAt;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private ContentStatus status;

    @Column(name = "is_duplicate")
    private Boolean isDuplicate = false;

    @Column(name = "duplicate_of_id")
    private Long duplicateOfId;

    @Column(name = "quality_score")
    private Integer qualityScore = 0;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "tags", length = 200)
    private String tags;

    @Column(name = "card_id")
    private Long cardId;

    @Column(name = "is_system_discovered")
    private Boolean isSystemDiscovered = false;

    public CrawledContent() {
        this.status = ContentStatus.RAW;
        this.crawledAt = LocalDateTime.now();
    }

    public enum ContentStatus {
        RAW,            // 原始抓取
        DEDUPLICATED,   // 已去重检查
        PROCESSING,     // AI处理中
        COMPLETED,      // 已生成卡片
        FAILED,         // 处理失败
        REJECTED        // 质量不合格
    }

    /**
     * 生成内容摘要（用于显示）
     */
    public String getSummary() {
        if (content == null || content.length() <= 200) {
            return content;
        }
        return content.substring(0, 200) + "...";
    }
}
