package com.evomind.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 新手引导状态实体
 */
@Entity
@Table(name = "onboarding_states")
@Getter
@Setter
public class OnboardingState extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "current_step", nullable = false)
    private Integer currentStep = 0;

    @Column(name = "total_steps", nullable = false)
    private Integer totalSteps = 5;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "trial_started_at")
    private LocalDateTime trialStartedAt;

    @Column(name = "trial_expired_at")
    private LocalDateTime trialExpiredAt;

    @Column(name = "is_trial_active", nullable = false)
    private Boolean isTrialActive = false;

    @Column(name = "skipped_steps", length = 100)
    private String skippedSteps; // 存储跳过的步骤，如 "1,3"

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    /**
     * 检查试用期是否有效
     */
    public boolean isTrialValid() {
        if (!isTrialActive || trialExpiredAt == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(trialExpiredAt);
    }

    /**
     * 获取剩余试用天数
     */
    public Integer getRemainingTrialDays() {
        if (!isTrialValid()) {
            return 0;
        }
        long days = java.time.Duration.between(LocalDateTime.now(), trialExpiredAt).toDays();
        return (int) Math.max(0, days);
    }

    /**
     * 进入下一步引导
     */
    public void nextStep() {
        if (currentStep < totalSteps) {
            currentStep++;
        }
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * 跳过当前步骤
     */
    public void skipStep(Integer step) {
        if (skippedSteps == null || skippedSteps.isEmpty()) {
            skippedSteps = step.toString();
        } else {
            skippedSteps += "," + step;
        }
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * 标记引导完成
     */
    public void markCompleted() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
        this.currentStep = totalSteps;
    }

    /**
     * 启动试用期（7天）
     */
    public void startTrial() {
        this.trialStartedAt = LocalDateTime.now();
        this.trialExpiredAt = LocalDateTime.now().plusDays(7);
        this.isTrialActive = true;
    }
}
