package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户阅读历史实体
 * 记录用户对卡片的阅读行为，用于构建用户画像和推荐
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_reading_histories")
public class UserReadingHistory extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "read_duration_seconds")
    private Integer readDurationSeconds;

    @Column(name = "read_percentage")
    private Integer readPercentage;

    @Column(name = "is_favorite")
    private Boolean isFavorite;

    @Column(name = "is_archived")
    private Boolean isArchived;

    @Column(name = "interaction_type")
    @Enumerated(EnumType.STRING)
    private InteractionType interactionType;

    @Column(name = "keywords", length = 500)
    private String keywords;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public UserReadingHistory() {
        this.readDurationSeconds = 0;
        this.readPercentage = 0;
        this.isFavorite = false;
        this.isArchived = false;
        this.interactionType = InteractionType.VIEW;
    }

    /**
     * 计算阅读质量得分
     */
    public Integer calculateQualityScore() {
        int score = 0;

        // 阅读时长得分 (最高40分)
        score += Math.min(readDurationSeconds / 10, 40);

        // 阅读完成度得分 (最高30分)
        score += (readPercentage * 30) / 100;

        // 互动行为加分
        if (isFavorite) score += 20;
        if (interactionType == InteractionType.SHARE) score += 10;
        if (interactionType == InteractionType.COMMENT) score += 10;

        return Math.min(score, 100);
    }

    public enum InteractionType {
        VIEW,       // 浏览
        READ,       // 阅读
        SHARE,      // 分享
        COMMENT,    // 评论
        SAVE        // 保存
    }
}
