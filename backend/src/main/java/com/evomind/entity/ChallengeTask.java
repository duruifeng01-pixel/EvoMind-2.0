package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 挑战任务实体
 * 系统每日发布的挑战任务
 */
@Entity
@Table(name = "challenge_tasks")
@Getter
@Setter
public class ChallengeTask extends BaseEntity {

    @Column(name = "date_key", nullable = false, length = 8, unique = true)
    private String dateKey; // 日期格式：20250214

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "task_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @Column(name = "target_count", nullable = false)
    private Integer targetCount; // 目标完成次数/数量

    @Column(name = "reward_points", nullable = false)
    private Integer rewardPoints = 10; // 完成奖励积分

    @Column(name = "reward_trial_days")
    private Integer rewardTrialDays = 0; // 额外奖励体验天数

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public enum TaskType {
        READ_CARDS,         // 阅读N张认知卡片
        ADD_SOURCES,        // 添加N个信息源
        CREATE_NOTES,       // 创建N条笔记
        COMPLETE_DISCUSSION,// 参与今日讨论
        SHARE_INSIGHT,      // 分享一条洞见
        DAILY_CHECKIN       // 每日签到
    }

    /**
     * 检查是否是今日任务
     */
    public boolean isToday() {
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return today.equals(this.dateKey);
    }
}
