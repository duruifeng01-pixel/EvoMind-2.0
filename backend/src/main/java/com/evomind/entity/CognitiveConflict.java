package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 认知冲突实体
 * 记录新卡片与用户认知画像的冲突
 */
@Entity
@Table(name = "cognitive_conflicts")
@Getter
@Setter
public class CognitiveConflict extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "conflict_type", length = 50)
    private String conflictType;

    @Column(name = "conflict_description", length = 500)
    private String conflictDescription;

    @Column(name = "user_belief", length = 1000)
    private String userBelief;

    @Column(name = "card_viewpoint", length = 1000)
    private String cardViewpoint;

    @Column(name = "conflict_score", precision = 5, scale = 4)
    private BigDecimal conflictScore;

    @Column(name = "is_acknowledged")
    private Boolean isAcknowledged = false;

    @Column(name = "is_dismissed")
    private Boolean isDismissed = false;

    @Column(name = "ai_analysis", length = 2000)
    private String aiAnalysis;

    @Column(name = "topic", length = 100)
    private String topic;
}
