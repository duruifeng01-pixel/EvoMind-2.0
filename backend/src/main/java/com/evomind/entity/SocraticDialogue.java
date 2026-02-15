package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 苏格拉底式对话会话实体
 * 记录用户与AI的苏格拉底式对话
 */
@Entity
@Table(name = "socratic_dialogues")
@Getter
@Setter
public class SocraticDialogue extends BaseEntity {

    @Column(name = "discussion_id", nullable = false)
    private Long discussionId; // 关联的讨论主题ID

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DialogueStatus status = DialogueStatus.IN_PROGRESS;

    @Column(name = "current_round")
    private Integer currentRound = 0; // 当前对话轮次

    @Column(name = "max_rounds")
    private Integer maxRounds = 5; // 最大对话轮次

    @Column(name = "initial_question", columnDefinition = "TEXT")
    private String initialQuestion; // 初始问题

    @Column(name = "final_insight", columnDefinition = "TEXT")
    private String finalInsight; // 最终洞察总结

    @Column(name = "insight_generated_at")
    private LocalDateTime insightGeneratedAt;

    @Column(name = "user_satisfaction")
    private Integer userSatisfaction; // 用户满意度评分 1-5

    @Column(name = "is_abandoned")
    private Boolean isAbandoned = false; // 是否被用户放弃

    @Column(name = "abandoned_at")
    private LocalDateTime abandonedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "total_messages")
    private Integer totalMessages = 0;

    @Version
    private Long version; // 乐观锁，防止并发修改

    /**
     * 对话状态枚举
     */
    public enum DialogueStatus {
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        ABANDONED("已放弃"),
        INSIGHT_GENERATED("洞察已生成");

        private final String description;

        DialogueStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 进入下一轮对话
     */
    public void nextRound() {
        this.currentRound++;
        this.lastMessageAt = LocalDateTime.now();
    }

    /**
     * 检查是否达到最大轮次
     */
    public boolean hasReachedMaxRounds() {
        return this.currentRound >= this.maxRounds;
    }

    /**
     * 标记对话完成
     */
    public void markCompleted() {
        this.status = DialogueStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 标记对话放弃
     */
    public void markAbandoned() {
        this.status = DialogueStatus.ABANDONED;
        this.isAbandoned = true;
        this.abandonedAt = LocalDateTime.now();
    }

    /**
     * 标记洞察已生成
     */
    public void markInsightGenerated(String insight) {
        this.finalInsight = insight;
        this.insightGeneratedAt = LocalDateTime.now();
        this.status = DialogueStatus.INSIGHT_GENERATED;
    }

    /**
     * 增加消息计数
     */
    public void incrementMessageCount() {
        this.totalMessages++;
    }

    /**
     * 检查是否可以继续对话
     */
    public boolean canContinue() {
        return this.status == DialogueStatus.IN_PROGRESS && !hasReachedMaxRounds();
    }
}
