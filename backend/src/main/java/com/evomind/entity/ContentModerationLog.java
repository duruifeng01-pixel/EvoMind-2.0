package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 内容审核日志实体
 * 记录所有AI生成内容和用户发布内容的审核结果
 * 符合国内AIGC合规要求
 */
@Data
@Entity
@Table(name = "content_moderation_logs")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentModerationLog extends BaseEntity {

    /**
     * 内容类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 50)
    private ContentType contentType;

    /**
     * 内容ID（关联的具体内容ID）
     */
    @Column(name = "content_id", length = 100)
    private String contentId;

    /**
     * 原始内容（脱敏存储）
     */
    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;

    /**
     * 内容摘要（用于日志展示）
     */
    @Column(name = "content_summary", length = 500)
    private String contentSummary;

    /**
     * 审核状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false, length = 20)
    private ModerationStatus moderationStatus;

    /**
     * 审核方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_type", nullable = false, length = 20)
    private ModerationType moderationType;

    /**
     * 违规类型（如审核不通过）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", length = 50)
    private ViolationType violationType;

    /**
     * 违规详情说明
     */
    @Column(name = "violation_details", length = 1000)
    private String violationDetails;

    /**
     * 敏感词命中列表（JSON格式）
     */
    @Column(name = "hit_sensitive_words", columnDefinition = "TEXT")
    private String hitSensitiveWords;

    /**
     * 审核服务商（百度/阿里/本地）
     */
    @Column(name = "provider", length = 50)
    private String provider;

    /**
     * 第三方API返回的原始结果（JSON）
     */
    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    /**
     * 审核请求ID（用于追溯）
     */
    @Column(name = "request_id", length = 100)
    private String requestId;

    /**
     * 是否AI生成内容
     */
    @Column(name = "is_ai_generated", nullable = false)
    private Boolean isAiGenerated = false;

    /**
     * AI模型名称（如果是AI生成）
     */
    @Column(name = "ai_model", length = 100)
    private String aiModel;

    /**
     * 审核耗时（毫秒）
     */
    @Column(name = "process_time_ms")
    private Long processTimeMs;

    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * 人工复核标记
     */
    @Column(name = "manual_review_required")
    private Boolean manualReviewRequired = false;

    /**
     * 人工复核结果
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "manual_review_result", length = 20)
    private ModerationStatus manualReviewResult;

    /**
     * 复核人ID
     */
    @Column(name = "reviewer_id")
    private Long reviewerId;

    /**
     * 复核时间
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * 复核备注
     */
    @Column(name = "review_remark", length = 500)
    private String reviewRemark;

    /**
     * 内容类型枚举
     */
    public enum ContentType {
        CARD_AI_SUMMARY,      // AI生成的卡片摘要
        CARD_AI_INSIGHT,      // AI生成的卡片洞察
        CARD_AI_MIND_MAP,     // AI生成的脑图
        SOCRATIC_DIALOGUE,    // 苏格拉底对话
        USER_CORPUS,          // 用户语料库内容
        VOICE_NOTE,           // 语音笔记转录
        USER_COMMENT,         // 用户评论
        USER_PROFILE,         // 用户资料
        CHAT_MESSAGE,         // 聊天消息
        SYSTEM_MESSAGE        // 系统消息
    }

    /**
     * 审核状态枚举
     */
    public enum ModerationStatus {
        PENDING,      // 待审核
        PROCESSING,   // 审核中
        APPROVED,     // 审核通过
        REJECTED,     // 审核不通过
        NEED_REVIEW,  // 需要人工复核
        ERROR         // 审核异常
    }

    /**
     * 审核方式枚举
     */
    public enum ModerationType {
        AUTO_SENSITIVE_WORD,   // 自动敏感词检测
        AUTO_BAIDU_API,        // 百度内容审核API
        AUTO_ALIYUN_API,       // 阿里云内容审核API
        MANUAL_REVIEW,         // 人工审核
        HYBRID                 // 混合审核
    }

    /**
     * 违规类型枚举
     */
    public enum ViolationType {
        NONE,                  // 无违规
        POLITICS,              // 政治敏感
        PORNOGRAPHY,           // 色情
        VIOLENCE,              // 暴力
        TERRORISM,             // 恐怖主义
        GAMBLING,              // 赌博
        FRAUD,                 // 诈骗
        ADVERTISEMENT,         // 广告违规
        ABUSE,                 // 辱骂
        PRIVACY,               // 隐私泄露
        INTELLECTUAL_PROPERTY, // 知识产权
        SENSITIVE_WORD,        // 敏感词命中
        OTHER                  // 其他违规
    }

    /**
     * 获取审核结果描述
     */
    public String getStatusDescription() {
        return switch (moderationStatus) {
            case PENDING -> "等待审核";
            case PROCESSING -> "审核中";
            case APPROVED -> "审核通过";
            case REJECTED -> "审核不通过";
            case NEED_REVIEW -> "需要人工复核";
            case ERROR -> "审核异常";
        };
    }

    /**
     * 是否审核通过
     */
    public boolean isApproved() {
        return moderationStatus == ModerationStatus.APPROVED;
    }

    /**
     * 是否需要阻断内容展示
     */
    public boolean shouldBlock() {
        return moderationStatus == ModerationStatus.REJECTED ||
               (moderationStatus == ModerationStatus.NEED_REVIEW && manualReviewRequired);
    }
}