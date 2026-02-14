package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户任务进度实体
 * 记录用户每日任务的完成情况
 */
@Entity
@Table(name = "user_task_progress")
@Getter
@Setter
public class UserTaskProgress extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "date_key", nullable = false, length = 8)
    private String dateKey; // 日期格式：20250214

    @Column(name = "current_count", nullable = false)
    private Integer currentCount = 0; // 当前完成进度

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "reward_claimed", nullable = false)
    private Boolean rewardClaimed = false; // 奖励是否已领取

    @Column(name = "reward_claimed_at")
    private LocalDateTime rewardClaimedAt;

    /**
     * 增加完成进度
     */
    public void incrementProgress(int amount, int targetCount) {
        this.currentCount += amount;
        if (this.currentCount >= targetCount && !this.isCompleted) {
            this.isCompleted = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * 领取奖励
     */
    public void claimReward() {
        if (!this.isCompleted) {
            throw new IllegalStateException("任务未完成，无法领取奖励");
        }
        if (this.rewardClaimed) {
            throw new IllegalStateException("奖励已领取");
        }
        this.rewardClaimed = true;
        this.rewardClaimedAt = LocalDateTime.now();
    }

    /**
     * 计算完成百分比
     */
    public int getProgressPercent(int targetCount) {
        if (targetCount <= 0) return 0;
        return Math.min(100, (this.currentCount * 100) / targetCount);
    }
}
