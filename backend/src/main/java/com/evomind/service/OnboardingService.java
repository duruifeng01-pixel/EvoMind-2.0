package com.evomind.service;

import com.evomind.dto.response.OnboardingStateResponse;

/**
 * 新手引导服务接口
 * 管理用户的新手引导流程和7天体验权益
 */
public interface OnboardingService {

    /**
     * 获取用户的新手引导状态
     * 如果不存在则自动创建
     *
     * @param userId 用户ID
     * @return 引导状态响应
     */
    OnboardingStateResponse getOnboardingState(Long userId);

    /**
     * 更新当前步骤
     *
     * @param userId 用户ID
     * @param step   当前步骤
     * @return 更新后的引导状态
     */
    OnboardingStateResponse updateStep(Long userId, Integer step);

    /**
     * 跳过指定步骤
     *
     * @param userId 用户ID
     * @param step   要跳过的步骤
     * @return 更新后的引导状态
     */
    OnboardingStateResponse skipStep(Long userId, Integer step);

    /**
     * 完成新手引导
     * 激活7天体验权益
     *
     * @param userId 用户ID
     * @return 完成后的引导状态（包含体验权益信息）
     */
    OnboardingStateResponse completeOnboarding(Long userId);

    /**
     * 检查用户是否有有效的体验权益
     *
     * @param userId 用户ID
     * @return true表示体验权益有效
     */
    boolean hasActiveTrial(Long userId);

    /**
     * 获取剩余体验天数
     *
     * @param userId 用户ID
     * @return 剩余天数，0表示已过期或未激活
     */
    Integer getRemainingTrialDays(Long userId);

    /**
     * 标记引导步骤为已完成
     *
     * @param userId     用户ID
     * @param stepNumber 步骤编号
     * @return 更新后的引导状态
     */
    OnboardingStateResponse markStepCompleted(Long userId, Integer stepNumber);
}
