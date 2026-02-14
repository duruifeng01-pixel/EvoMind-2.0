package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "card_conflicts")
@Getter
@Setter
public class CardConflict extends BaseEntity {

    @Column(name = "card_id_1", nullable = false)
    private Long cardId1;

    @Column(name = "card_id_2", nullable = false)
    private Long cardId2;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "conflict_type", length = 50)
    private String conflictType;

    @Column(name = "conflict_description", length = 500)
    private String conflictDescription;

    @Column(name = "topic", length = 100)
    private String topic;

    @Column(name = "similarity_score", precision = 5, scale = 4)
    private BigDecimal similarityScore;

    @Column(name = "conflict_score", precision = 5, scale = 4)
    private BigDecimal conflictScore;

    @Column(name = "is_acknowledged")
    private Boolean isAcknowledged = false;

    @Column(name = "ai_analysis", length = 2000)
    private String aiAnalysis;
}
