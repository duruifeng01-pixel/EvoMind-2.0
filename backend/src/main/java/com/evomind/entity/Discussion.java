package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 每日讨论主题实体
 * 每日自动生成的讨论话题
 */
@Entity
@Table(name = "discussions")
@Getter
@Setter
public class Discussion extends BaseEntity {

    @Column(name = "date_key", nullable = false, length = 8, unique = true)
    private String dateKey; // 日期格式：20250214

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "topic_tag", length = 50)
    private String topicTag;

    @Column(name = "related_source_ids", length = 500)
    private String relatedSourceIds; // 关联的信息源ID列表，逗号分隔

    @Column(name = "related_card_ids", length = 500)
    private String relatedCardIds; // 关联的认知卡片ID列表

    @Column(name = "ai_prompt", columnDefinition = "TEXT")
    private String aiPrompt; // AI对话的Prompt模板

    @Column(name = "participant_count")
    private Integer participantCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_by")
    private Long createdBy; // 系统生成则为null

    /**
     * 检查是否是今天的讨论
     */
    public boolean isToday() {
        String today = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return today.equals(this.dateKey);
    }

    /**
     * 增加参与者数量
     */
    public void incrementParticipant() {
        this.participantCount = (this.participantCount == null ? 0 : this.participantCount) + 1;
    }

    /**
     * 增加评论数量
     */
    public void incrementComment() {
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) + 1;
    }
}
