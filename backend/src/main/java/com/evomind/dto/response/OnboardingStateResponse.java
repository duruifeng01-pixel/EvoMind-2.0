package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新手引导状态响应DTO
 */
@Data
@Schema(description = "新手引导状态响应")
public class OnboardingStateResponse {

    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @Schema(description = "当前步骤", example = "2")
    private Integer currentStep;

    @Schema(description = "总步骤数", example = "5")
    private Integer totalSteps;

    @Schema(description = "是否已完成引导", example = "false")
    private Boolean isCompleted;

    @Schema(description = "进度百分比", example = "40")
    private Integer progressPercent;

    @Schema(description = "体验权益是否有效", example = "true")
    private Boolean trialActive;

    @Schema(description = "体验开始时间")
    private LocalDateTime trialStartedAt;

    @Schema(description = "体验过期时间")
    private LocalDateTime trialExpiredAt;

    @Schema(description = "剩余体验天数", example = "5")
    private Integer remainingTrialDays;
}
