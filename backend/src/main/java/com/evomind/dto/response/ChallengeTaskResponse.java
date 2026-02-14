package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 挑战任务响应DTO
 */
@Data
@Schema(description = "挑战任务响应")
public class ChallengeTaskResponse {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "日期Key")
    private String dateKey;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "目标数量")
    private Integer targetCount;

    @Schema(description = "奖励积分")
    private Integer rewardPoints;

    @Schema(description = "额外奖励体验天数")
    private Integer rewardTrialDays;

    // 用户进度相关
    @Schema(description = "当前进度")
    private Integer currentCount;

    @Schema(description = "进度百分比")
    private Integer progressPercent;

    @Schema(description = "是否已完成")
    private Boolean isCompleted;

    @Schema(description = "奖励是否已领取")
    private Boolean rewardClaimed;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "是否是今日任务")
    private Boolean isToday;
}
