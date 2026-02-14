package com.evomind.service.impl;

import com.evomind.dto.response.OnboardingStateResponse;
import com.evomind.entity.OnboardingState;
import com.evomind.exception.ResourceNotFoundException;
import com.evomind.repository.OnboardingStateRepository;
import com.evomind.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 新手引导服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final OnboardingStateRepository onboardingStateRepository;

    // 总步骤数
    private static final int TOTAL_STEPS = 5;

    // 体验权益天数
    private static final int TRIAL_DAYS = 7;

    @Override
    @Transactional(readOnly = true)
    public OnboardingStateResponse getOnboardingState(Long userId) {
        OnboardingState state = onboardingStateRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultState(userId));
        return convertToResponse(state);
    }

    @Override
    @Transactional
    public OnboardingStateResponse updateStep(Long userId, Integer step) {
        OnboardingState state = onboardingStateRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultState(userId));

        // 验证步骤有效性
        if (step < 0 || step > TOTAL_STEPS) {
            throw new IllegalArgumentException("无效的步骤编号: " + step);
        }

        // 只能向前推进，不能回退
        if (step > state.getCurrentStep()) {
            state.setCurrentStep(step);
        }

        // 如果到达最后一步，自动标记完成
        if (step >= TOTAL_STEPS && !state.getIsCompleted()) {
            state.setIsCompleted(true);
            // 自动激活体验权益
            if (state.getTrialStartedAt() == null) {
                activateTrial(state);
            }
        }

        OnboardingState saved = onboardingStateRepository.save(state);
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public OnboardingStateResponse skipStep(Long userId, Integer step) {
        OnboardingState state = onboardingStateRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultState(userId));

        // 标记该步骤为已跳过
        state.skipStep(step);

        // 如果跳过的是当前步骤，自动推进到下一步
        if (step.equals(state.getCurrentStep())) {
            state.nextStep();
        }

        OnboardingState saved = onboardingStateRepository.save(state);
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public OnboardingStateResponse completeOnboarding(Long userId) {
        OnboardingState state = onboardingStateRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultState(userId));

        // 标记为已完成
        state.markCompleted();

        // 激活7天体验权益
        if (state.getTrialStartedAt() == null) {
            activateTrial(state);
            log.info("用户 {} 完成新手引导，激活7天体验权益", userId);
        }

        OnboardingState saved = onboardingStateRepository.save(state);
        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveTrial(Long userId) {
        Optional<OnboardingState> stateOpt = onboardingStateRepository.findByUserId(userId);
        if (stateOpt.isEmpty()) {
            return false;
        }
        return stateOpt.get().isTrialValid();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getRemainingTrialDays(Long userId) {
        Optional<OnboardingState> stateOpt = onboardingStateRepository.findByUserId(userId);
        if (stateOpt.isEmpty()) {
            return 0;
        }
        return stateOpt.get().getRemainingTrialDays();
    }

    @Override
    @Transactional
    public OnboardingStateResponse markStepCompleted(Long userId, Integer stepNumber) {
        OnboardingState state = onboardingStateRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultState(userId));

        // 如果完成的是当前步骤，自动推进
        if (stepNumber.equals(state.getCurrentStep())) {
            state.nextStep();
        }

        // 如果所有步骤都完成了，标记整体完成
        if (state.getCurrentStep() >= TOTAL_STEPS && !state.getIsCompleted()) {
            state.markCompleted();
            if (state.getTrialStartedAt() == null) {
                activateTrial(state);
            }
        }

        OnboardingState saved = onboardingStateRepository.save(state);
        return convertToResponse(saved);
    }

    /**
     * 创建默认的引导状态
     */
    private OnboardingState createDefaultState(Long userId) {
        OnboardingState state = new OnboardingState();
        state.setUserId(userId);
        state.setCurrentStep(0);
        state.setTotalSteps(TOTAL_STEPS);
        state.setIsCompleted(false);
        state.setIsTrialActive(false);
        state.setTrialStartedAt(null);
        state.setTrialExpiredAt(null);
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        return onboardingStateRepository.save(state);
    }

    /**
     * 激活7天体验权益
     */
    private void activateTrial(OnboardingState state) {
        state.setTrialStartedAt(LocalDateTime.now());
        state.setTrialExpiredAt(LocalDateTime.now().plusDays(TRIAL_DAYS));
        state.setIsTrialActive(true);
        state.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * 转换为响应DTO
     */
    private OnboardingStateResponse convertToResponse(OnboardingState state) {
        OnboardingStateResponse response = new OnboardingStateResponse();
        response.setUserId(state.getUserId());
        response.setCurrentStep(state.getCurrentStep());
        response.setTotalSteps(state.getTotalSteps());
        response.setIsCompleted(state.getIsCompleted());
        response.setTrialActive(state.isTrialValid());
        response.setTrialStartedAt(state.getTrialStartedAt());
        response.setTrialExpiredAt(state.getTrialExpiredAt());
        response.setRemainingTrialDays(state.getRemainingTrialDays());

        // 计算当前步骤进度百分比
        int progressPercent = (state.getCurrentStep() * 100) / state.getTotalSteps();
        response.setProgressPercent(progressPercent);

        return response;
    }
}
