package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import com.evomind.dto.request.SubmitArtifactRequest;
import com.evomind.dto.response.ChallengeTaskResponse;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.ChallengeTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 挑战任务控制器
 * 每日挑战任务相关API - 符合API契约 v1
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
@Tag(name = "挑战任务", description = "每日挑战任务、任务状态、作品提交相关接口")
public class ChallengeTaskController {

    private final ChallengeTaskService challengeTaskService;

    @Operation(summary = "获取当前挑战任务", description = "获取当天的挑战任务及用户进度")
    @GetMapping("/current")
    public ApiResponse<ChallengeTaskResponse> getCurrentChallenge(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ChallengeTaskResponse response = challengeTaskService.getTodayTask(userDetails.getId());
        return ApiResponse.success("获取成功", response);
    }

    @Operation(summary = "获取任务详情", description = "获取指定任务的详细信息和进度")
    @GetMapping("/{id}")
    public ApiResponse<ChallengeTaskResponse> getChallenge(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        ChallengeTaskResponse response = challengeTaskService.getTaskProgress(userDetails.getId(), id);
        return ApiResponse.success("获取成功", response);
    }

    @Operation(summary = "更新任务状态", description = "更新任务完成状态（手动确认完成）")
    @PostMapping("/{id}/status")
    public ApiResponse<ChallengeTaskResponse> updateChallengeStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID", required = true) @PathVariable Long id,
            @Parameter(description = "是否完成", example = "true") @RequestParam Boolean completed) {
        ChallengeTaskResponse response;
        if (Boolean.TRUE.equals(completed)) {
            // 标记为完成，进度设为最大值
            response = challengeTaskService.updateProgress(userDetails.getId(), id, 100);
        } else {
            // 仅查询当前状态
            response = challengeTaskService.getTaskProgress(userDetails.getId(), id);
        }
        return ApiResponse.success("状态更新成功", response);
    }

    @Operation(summary = "提交作品", description = "提交任务完成的作品/成果")
    @PostMapping("/{id}/artifact")
    public ApiResponse<ChallengeTaskResponse> submitArtifact(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID", required = true) @PathVariable Long id,
            @Valid @RequestBody SubmitArtifactRequest request) {
        // 提交作品同时更新任务进度
        ChallengeTaskResponse response = challengeTaskService.submitArtifact(
                userDetails.getId(), id, request);
        return ApiResponse.success("作品提交成功", response);
    }

    @Operation(summary = "领取任务奖励", description = "任务完成后领取积分和体验天数奖励")
    @PostMapping("/{id}/claim")
    public ApiResponse<ChallengeTaskResponse> claimReward(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        ChallengeTaskResponse response = challengeTaskService.claimReward(userDetails.getId(), id);
        return ApiResponse.success("奖励领取成功", response);
    }

    @Operation(summary = "记录用户活动", description = "记录用户活动用于自动更新任务进度（内部调用）")
    @PostMapping("/activity")
    public ApiResponse<Void> recordActivity(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "活动类型", required = true, example = "READ_CARD")
            @RequestParam ChallengeTaskService.ActivityType activityType) {
        challengeTaskService.recordUserActivity(userDetails.getId(), activityType);
        return ApiResponse.success("活动记录成功", null);
    }
}
