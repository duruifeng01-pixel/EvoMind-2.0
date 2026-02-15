package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 苏格拉底式对话消息实体
 * 记录对话中的每一条消息
 */
@Entity
@Table(name = "socratic_messages")
@Getter
@Setter
public class SocraticMessage extends BaseEntity {

    @Column(name = "dialogue_id", nullable = false)
    private Long dialogueId; // 关联的对话会话ID

    @Column(nullable = false)
    private Integer round; // 对话轮次

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MessageRole role; // 消息角色

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 消息内容

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private MessageType type; // 消息类型

    @Column(name = "question_category", length = 50)
    private String questionCategory; // 问题类别（用于分析）

    @Column(name = "depth_level")
    private Integer depthLevel = 1; // 追问深度层级 1-5

    @Column(name = "is_follow_up")
    private Boolean isFollowUp = false; // 是否是追问

    @Column(name = "follow_up_target_id")
    private Long followUpTargetId; // 追问的目标消息ID

    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis; // AI对用户回答的分析（仅AI消息）

    @Column(name = "thinking_hints", columnDefinition = "TEXT")
    private String thinkingHints; // 思考提示（给用户的问题引导）

    @Column(name = "key_points_extracted", columnDefinition = "TEXT")
    private String keyPointsExtracted; // 从用户回答中提取的关键点

    @Column(name = "user_intent", length = 100)
    private String userIntent; // 用户意图分析

    @Column(name = "sentiment_score")
    private Double sentimentScore; // 情感分数 -1.0 到 1.0

    @Column(name = "response_time_seconds")
    private Integer responseTimeSeconds; // AI响应时间（秒）

    @Column(name = "token_count")
    private Integer tokenCount; // Token使用量

    @Column(name = "sequence_number")
    private Integer sequenceNumber; // 消息序号

    @Column(name = "is_final_summary")
    private Boolean isFinalSummary = false; // 是否是最终总结

    @Column(name = "branch_from_message_id")
    private Long branchFromMessageId; // 分支来源消息ID（支持对话分支）

    /**
     * 消息角色枚举
     */
    public enum MessageRole {
        AI("AI引导者"),
        USER("用户");

        private final String description;

        MessageRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        INITIAL_QUESTION("初始问题"),
        FOLLOW_UP_QUESTION("追问"),
        CLARIFYING_QUESTION("澄清问题"),
        DEEPENING_QUESTION("深化问题"),
        CHALLENGE_QUESTION("挑战问题"),
        REFLECTIVE_QUESTION("反思问题"),
        USER_RESPONSE("用户回应"),
        USER_QUESTION("用户提问"),
        INSIGHT_SUMMARY("洞察总结"),
        TRANSITION("过渡引导"),
        ENCOURAGEMENT("鼓励");

        private final String description;

        MessageType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isQuestion() {
            return this == INITIAL_QUESTION ||
                   this == FOLLOW_UP_QUESTION ||
                   this == CLARIFYING_QUESTION ||
                   this == DEEPENING_QUESTION ||
                   this == CHALLENGE_QUESTION ||
                   this == REFLECTIVE_QUESTION;
        }
    }

    /**
     * 判断是否是AI发送的消息
     */
    public boolean isFromAi() {
        return this.role == MessageRole.AI;
    }

    /**
     * 判断是否是用户发送的消息
     */
    public boolean isFromUser() {
        return this.role == MessageRole.USER;
    }

    /**
     * 获取消息预览（前100字）
     */
    public String getPreview() {
        if (content == null || content.length() <= 100) {
            return content;
        }
        return content.substring(0, 100) + "...";
    }

    /**
     * 增加深度层级
     */
    public void deepen() {
        if (this.depthLevel < 5) {
            this.depthLevel++;
        }
    }
}
