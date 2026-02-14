package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Getter
@Setter
public class Card extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "summary_text", length = 2000)
    private String summaryText;

    @Column(name = "one_sentence_summary", length = 200)
    private String oneSentenceSummary;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    @Column(name = "source_title", length = 200)
    private String sourceTitle;

    @Column(name = "original_content_id")
    private Long originalContentId;

    @Column(name = "mindmap_json", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String mindmapJson;

    @Column(name = "is_favorite")
    private Boolean isFavorite = false;

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "has_conflict")
    private Boolean hasConflict = false;

    @Column(name = "conflict_card_ids")
    private String conflictCardIds;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "ai_model", length = 64)
    private String aiModel;

    @Column(name = "token_used")
    private Integer tokenUsed = 0;

    @Column(name = "generate_status", length = 20)
    private String generateStatus = "PENDING";

    @Column(name = "keywords")
    private String keywords;

    @Column(name = "reading_time_minutes")
    private Integer readingTimeMinutes;
}
