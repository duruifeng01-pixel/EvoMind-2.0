package com.evomind.controller;

import com.evomind.dto.response.ChallengeTaskResponse;
import com.evomind.dto.response.Result;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.ChallengeTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 挑战任务控制器
 * 每日挑战任务相关API
 */
@Slf4j
@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "挑战任务", description = "每日挑战任务、任务进度、奖励领取相关接口")
public class ChallengeTaskController {

    private final ChallengeTaskService challengeTaskService;

    @Operation(summary = "获取今日挑战任务", description = "获取当天的挑战任务及用户进度")
    @GetMapping("/today")
    public Result<ChallengeTaskResponse> getTodayTask(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ChallengeTaskResponse response = challengeTaskService.getTodayTask(userDetails.getId());
        return Result.success(response);
    }

    @Operation(summary = "获取任务进度", description = "获取指定任务的详细进度")
    @GetMapping("/{taskId}/progress")
    public Result<ChallengeTaskResponse> getTaskProgress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        ChallengeTaskResponse response = challengeTaskService.getTaskProgress(userDetails.getId(), taskId);
        return Result.success(response);
    }

    @Operation(summary = "手动更新进度", description = "手动更新任务进度（通常由系统自动调用）")
    @PostMapping("/{taskId}/progress")
    public Result<ChallengeTaskResponse> updateProgress(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId,
            @Parameter(description = "增加的进度值", example = "1") @RequestParam(defaultValue = "1") int increment) {
        ChallengeTaskResponse response = challengeTaskService.updateProgress(userDetails.getId(), taskId, increment);
        return Result.success(response);
    }

    @Operation(summary = "领取任务奖励", description = "任务完成后领取积分和体验天数奖励")
    @PostMapping("/{taskId}/claim")
    public Result<ChallengeTaskResponse> claimReward(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        ChallengeTaskResponse response = challengeTaskService.claimReward(userDetails.getId(), taskId);
        return Result.success(response);
    }

    @Operation(summary = "获取未领取的奖励列表", description = "获取用户已完成但未领取奖励的任务列表")
    @GetMapping("/unclaimed")
    public Result<List<ChallengeTaskResponse>> getUnclaimedRewards(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ChallengeTaskResponse> rewards = challengeTaskService.getUnclaimedRewards(userDetails.getId());
        return Result.success(rewards);
    }

    @Operation(summary = "记录用户活动", description = "记录用户活动用于自动更新任务进度（内部调用）")
    @PostMapping("/activity")
    public Result<Void> recordActivity(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "活动类型", required = true, example = "READ_CARD")
            @RequestParam ChallengeTaskService.ActivityType activityType) {
        challengeTaskService.recordUserActivity(userDetails.getId(), activityType);
        return Result.success();
    }
}
