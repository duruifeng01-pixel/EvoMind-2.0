package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户每日Feed配额实体
 * 追踪用户每日阅读量和已读内容
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_daily_feed_quotas")
public class UserDailyFeedQuota extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quota_date", nullable = false)
    private LocalDate quotaDate;

    @Column(name = "daily_limit", nullable = false)
    private Integer dailyLimit = 300;

    @Column(name = "consumed_count", nullable = false)
    private Integer consumedCount = 0;

    @Column(name = "remaining_count", nullable = false)
    private Integer remainingCount = 300;

    @Column(name = "user_source_count")
    private Integer userSourceCount = 0;

    @Column(name = "recommended_count")
    private Integer recommendedCount = 0;

    @Column(name = "is_exhausted")
    private Boolean isExhausted = false;

    @Column(name = "exhausted_at")
    private LocalDateTime exhaustedAt;

    @Column(name = "last_reset_at")
    private LocalDateTime lastResetAt;

    public UserDailyFeedQuota() {
        this.quotaDate = LocalDate.now();
        this.remainingCount = this.dailyLimit;
    }

    /**
     * 消费配额
     */
    public boolean consume(int count, boolean isUserSource) {
        if (isExhausted || remainingCount <= 0) {
            return false;
        }

        int actualConsume = Math.min(count, remainingCount);
        consumedCount += actualConsume;
        remainingCount -= actualConsume;

        if (isUserSource) {
            userSourceCount += actualConsume;
        } else {
            recommendedCount += actualConsume;
        }

        if (remainingCount <= 0) {
            isExhausted = true;
            exhaustedAt = LocalDateTime.now();
        }

        return actualConsume > 0;
    }

    /**
     * 检查是否需要重置（新的一天）
     */
    public boolean needsReset() {
        return !quotaDate.equals(LocalDate.now());
    }

    /**
     * 重置配额
     */
    public void reset() {
        this.quotaDate = LocalDate.now();
        this.consumedCount = 0;
        this.remainingCount = this.dailyLimit;
        this.userSourceCount = 0;
        this.recommendedCount = 0;
        this.isExhausted = false;
        this.exhaustedAt = null;
        this.lastResetAt = LocalDateTime.now();
    }
}
