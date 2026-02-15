package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 算力成本记录实体
 * 记录用户各项AI服务消耗的算力成本
 */
@Data
@Entity
@Table(name = "computing_cost_records")
@EqualsAndHashCode(callSuper = true)
public class ComputingCostRecord extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    // === 成本指标 ===

    @Column(name = "source_count", nullable = false)
    private Integer sourceCount = 0;

    @Column(name = "conflict_mark_count", nullable = false)
    private Integer conflictMarkCount = 0;

    @Column(name = "ocr_request_count", nullable = false)
    private Integer ocrRequestCount = 0;

    @Column(name = "ai_token_count", nullable = false)
    private Long aiTokenCount = 0L;

    @Column(name = "dialogue_turn_count", nullable = false)
    private Integer dialogueTurnCount = 0;

    @Column(name = "model_training_count", nullable = false)
    private Integer modelTrainingCount = 0;

    @Column(name = "feed_card_count", nullable = false)
    private Integer feedCardCount = 0;

    @Column(name = "crawl_request_count", nullable = false)
    private Integer crawlRequestCount = 0;

    // === 成本金额（元）===

    @Column(name = "ocr_cost", nullable = false, precision = 10, scale = 4)
    private BigDecimal ocrCost = BigDecimal.ZERO;

    @Column(name = "ai_cost", nullable = false, precision = 10, scale = 4)
    private BigDecimal aiCost = BigDecimal.ZERO;

    @Column(name = "crawl_cost", nullable = false, precision = 10, scale = 4)
    private BigDecimal crawlCost = BigDecimal.ZERO;

    @Column(name = "storage_cost", nullable = false, precision = 10, scale = 4)
    private BigDecimal storageCost = BigDecimal.ZERO;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 4)
    private BigDecimal totalCost = BigDecimal.ZERO;

    // === 订阅费用计算 ===

    @Column(name = "subscription_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal subscriptionFee = BigDecimal.ZERO;

    @Column(name = "cost_multiplier", nullable = false)
    private Integer costMultiplier = 2; // 默认成本×2

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Version
    @Column(name = "version")
    private Long version = 0L;
}
