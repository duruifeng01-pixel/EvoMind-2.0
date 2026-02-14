package com.evomind.controller;

import com.evomind.dto.response.ApiResponse;
import com.evomind.dto.response.OnboardingStateResponse;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 新手引导控制器
 * 管理用户的新手引导流程和7天体验权益
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Tag(name = "新手引导", description = "新手引导流程和体验权益管理")
public class OnboardingController {

    private final OnboardingService onboardingService;

    /**
     * 获取当前用户的新手引导状态
     */
    @GetMapping("/state")
    @Operation(summary = "获取引导状态", description = "获取当前用户的新手引导状态和体验权益信息")
    public ResponseEntity<ApiResponse<OnboardingStateResponse>> getOnboardingState(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        log.debug("获取用户 {} 的引导状态", userId);
        
        OnboardingStateResponse state = onboardingService.getOnboardingState(userId);
        return ResponseEntity.ok(ApiResponse.success(state));
    }

    /**
     * 更新当前步骤
     */
    @PostMapping("/step/{step}")
    @Operation(summary = "更新步骤", description = "更新当前进行到的引导步骤")
    public ResponseEntity<ApiResponse<OnboardingStateResponse>> updateStep(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "步骤编号", example = "2")
            @PathVariable Integer step) {
        
        Long userId = userDetails.getId();
        log.info("用户 {} 更新引导步骤到 {}", userId, step);
        
        OnboardingStateResponse state = onboardingService.updateStep(userId, step);
        return ResponseEntity.ok(ApiResponse.success(state));
    }

    /**
     * 跳过指定步骤
     */
    @PostMapping("/step/{step}/skip")
    @Operation(summary = "跳过步骤", description = "跳过指定的引导步骤")
    public ResponseEntity<ApiResponse<OnboardingStateResponse>> skipStep(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "要跳过的步骤编号", example = "1")
            @PathVariable Integer step) {
        
        Long userId = userDetails.getId();
        log.info("用户 {} 跳过引导步骤 {}", userId, step);
        
        OnboardingStateResponse state = onboardingService.skipStep(userId, step);
        return ResponseEntity.ok(ApiResponse.success(state));
    }

    /**
     * 标记步骤为已完成
     */
    @PostMapping("/step/{step}/complete")
    @Operation(summary = "完成步骤", description = "标记指定步骤为已完成")
    public ResponseEntity<ApiResponse<OnboardingStateResponse>> completeStep(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "已完成的步骤编号", example = "1")
            @PathVariable Integer step) {
        
        Long userId = userDetails.getId();
        log.info("用户 {} 完成引导步骤 {}", userId, step);
        
        OnboardingStateResponse state = onboardingService.markStepCompleted(userId, step);
        return ResponseEntity.ok(ApiResponse.success(state));
    }

    /**
     * 完成整个新手引导
     */
    @PostMapping("/complete")
    @Operation(summary = "完成引导", description = "完成整个新手引导流程，激活7天体验权益")
    public ResponseEntity<ApiResponse<OnboardingStateResponse>> completeOnboarding(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        log.info("用户 {} 完成新手引导", userId);
        
        OnboardingStateResponse state = onboardingService.completeOnboarding(userId);
        return ResponseEntity.ok(ApiResponse.success(state));
    }

    /**
     * 检查体验权益状态
     */
    @GetMapping("/trial/status")
    @Operation(summary = "检查体验权益", description = "检查当前用户的7天体验权益是否有效")
    public ResponseEntity<ApiResponse<Boolean>> checkTrialStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        boolean hasActiveTrial = onboardingService.hasActiveTrial(userId);
        
        return ResponseEntity.ok(ApiResponse.success(hasActiveTrial));
    }

    /**
     * 获取剩余体验天数
     */
    @GetMapping("/trial/remaining-days")
    @Operation(summary = "获取剩余天数", description = "获取7天体验权益的剩余天数")
    public ResponseEntity<ApiResponse<Integer>> getRemainingTrialDays(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        Integer remainingDays = onboardingService.getRemainingTrialDays(userId);
        
        return ResponseEntity.ok(ApiResponse.success(remainingDays));
    }
}
