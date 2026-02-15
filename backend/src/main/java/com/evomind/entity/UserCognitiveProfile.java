package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 用户认知画像实体
 * 记录用户的核心认知立场和信念体系
 */
@Entity
@Table(name = "user_cognitive_profiles")
@Getter
@Setter
public class UserCognitiveProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "topic", length = 100, nullable = false)
    private String topic;

    @Column(name = "core_belief", length = 1000)
    private String coreBelief;

    @Column(name = "belief_type", length = 50)
    @Enumerated(EnumType.STRING)
    private BeliefType beliefType;

    @Column(name = "confidence_level", precision = 3, scale = 2)
    private BigDecimal confidenceLevel;

    @Column(name = "evidence_count")
    private Integer evidenceCount;

    @Column(name = "source_card_ids", length = 500)
    private String sourceCardIds;

    @Column(name = "keywords", length = 200)
    private String keywords;

    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * 信念类型
     */
    public enum BeliefType {
        STRONG_CONViction,    // 强信念（有多张卡片支持）
        MODERATE_STANCE,      // 中等立场（有一些证据）
        EXPLORING,            // 探索中（刚刚形成）
        TENTATIVE             // 暂定的（证据不足）
    }
}
