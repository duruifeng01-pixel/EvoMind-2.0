package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户兴趣画像实体
 * 记录用户的兴趣标签和权重，用于内容推荐
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_interest_profiles")
public class UserInterestProfile extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "interest_tag", nullable = false, length = 100)
    private String interestTag;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "weight", precision = 5, scale = 4)
    private BigDecimal weight;

    @Column(name = "source_count")
    private Integer sourceCount;

    @Column(name = "interaction_score")
    private Integer interactionScore;

    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;

    @Column(name = "is_active")
    private Boolean isActive;

    public UserInterestProfile() {
        this.weight = BigDecimal.valueOf(0.5);
        this.sourceCount = 0;
        this.interactionScore = 0;
        this.isActive = true;
    }

    /**
     * 计算综合得分
     */
    public BigDecimal calculateScore() {
        double baseWeight = weight.doubleValue();
        double sourceFactor = Math.min(sourceCount / 10.0, 1.0) * 0.3;
        double interactionFactor = Math.min(interactionScore / 100.0, 1.0) * 0.4;
        double recencyFactor = calculateRecencyFactor() * 0.3;

        return BigDecimal.valueOf(baseWeight + sourceFactor + interactionFactor + recencyFactor)
                .min(BigDecimal.ONE);
    }

    private double calculateRecencyFactor() {
        if (lastInteractionAt == null) return 0.0;

        long daysSinceInteraction = java.time.Duration.between(
                lastInteractionAt, LocalDateTime.now()
        ).toDays();

        if (daysSinceInteraction <= 1) return 1.0;
        if (daysSinceInteraction <= 7) return 0.8;
        if (daysSinceInteraction <= 30) return 0.5;
        return 0.2;
    }
}
