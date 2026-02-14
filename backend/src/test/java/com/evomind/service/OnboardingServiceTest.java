package com.evomind.service;

import com.evomind.dto.response.OnboardingStateResponse;
import com.evomind.entity.OnboardingState;
import com.evomind.repository.OnboardingStateRepository;
import com.evomind.service.impl.OnboardingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 新手引导服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private OnboardingStateRepository onboardingStateRepository;

    @InjectMocks
    private OnboardingServiceImpl onboardingService;

    private static final Long TEST_USER_ID = 10001L;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("获取引导状态 - 已存在状态")
    void getOnboardingState_ExistingState() {
        // Given
        OnboardingState state = createTestState();
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));

        // When
        OnboardingStateResponse response = onboardingService.getOnboardingState(TEST_USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getCurrentStep()).isEqualTo(2);
        assertThat(response.getIsCompleted()).isFalse();
    }

    @Test
    @DisplayName("获取引导状态 - 不存在则创建")
    void getOnboardingState_CreateNew() {
        // Given
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.empty());
        when(onboardingStateRepository.save(any(OnboardingState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OnboardingStateResponse response = onboardingService.getOnboardingState(TEST_USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getCurrentStep()).isEqualTo(0);
        assertThat(response.getTotalSteps()).isEqualTo(5);
        verify(onboardingStateRepository).save(any(OnboardingState.class));
    }

    @Test
    @DisplayName("更新步骤 - 正常前进")
    void updateStep_NormalProgress() {
        // Given
        OnboardingState state = createTestState();
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));
        when(onboardingStateRepository.save(any(OnboardingState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OnboardingStateResponse response = onboardingService.updateStep(TEST_USER_ID, 3);

        // Then
        assertThat(response.getCurrentStep()).isEqualTo(3);
        assertThat(response.getIsCompleted()).isFalse();
    }

    @Test
    @DisplayName("更新步骤 - 无效步骤抛出异常")
    void updateStep_InvalidStep() {
        // Given
        OnboardingState state = createTestState();
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));

        // Then
        assertThatThrownBy(() -> onboardingService.updateStep(TEST_USER_ID, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效的步骤编号");
    }

    @Test
    @DisplayName("更新步骤 - 完成最后一步自动标记完成")
    void updateStep_CompleteLastStep() {
        // Given
        OnboardingState state = createTestState();
        state.setCurrentStep(4);
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));
        when(onboardingStateRepository.save(any(OnboardingState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OnboardingStateResponse response = onboardingService.updateStep(TEST_USER_ID, 5);

        // Then
        assertThat(response.getIsCompleted()).isTrue();
        assertThat(response.getTrialActive()).isTrue();
    }

    @Test
    @DisplayName("跳过步骤")
    void skipStep() {
        // Given
        OnboardingState state = createTestState();
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));
        when(onboardingStateRepository.save(any(OnboardingState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OnboardingStateResponse response = onboardingService.skipStep(TEST_USER_ID, 3);

        // Then
        assertThat(response.getCurrentStep()).isEqualTo(3);
    }

    @Test
    @DisplayName("完成引导 - 激活7天体验")
    void completeOnboarding_ActivateTrial() {
        // Given
        OnboardingState state = createTestState();
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));
        when(onboardingStateRepository.save(any(OnboardingState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OnboardingStateResponse response = onboardingService.completeOnboarding(TEST_USER_ID);

        // Then
        assertThat(response.getIsCompleted()).isTrue();
        assertThat(response.getTrialActive()).isTrue();
        assertThat(response.getRemainingTrialDays()).isEqualTo(7);
    }

    @Test
    @DisplayName("检查体验权益 - 有效期内")
    void hasActiveTrial_Valid() {
        // Given
        OnboardingState state = createActiveTrialState();
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));

        // When
        boolean result = onboardingService.hasActiveTrial(TEST_USER_ID);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查体验权益 - 已过期")
    void hasActiveTrial_Expired() {
        // Given
        OnboardingState state = createExpiredTrialState();
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));

        // When
        boolean result = onboardingService.hasActiveTrial(TEST_USER_ID);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("获取剩余天数 - 有效期内")
    void getRemainingTrialDays_Valid() {
        // Given
        OnboardingState state = createActiveTrialState();
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));

        // When
        Integer days = onboardingService.getRemainingTrialDays(TEST_USER_ID);

        // Then
        assertThat(days).isEqualTo(7);
    }

    @Test
    @DisplayName("获取剩余天数 - 已过期")
    void getRemainingTrialDays_Expired() {
        // Given
        OnboardingState state = createExpiredTrialState();
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));

        // When
        Integer days = onboardingService.getRemainingTrialDays(TEST_USER_ID);

        // Then
        assertThat(days).isEqualTo(0);
    }

    @Test
    @DisplayName("标记步骤完成")
    void markStepCompleted() {
        // Given
        OnboardingState state = createTestState();
        when(onboardingStateRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(state));
        when(onboardingStateRepository.save(any(OnboardingState.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OnboardingStateResponse response = onboardingService.markStepCompleted(TEST_USER_ID, 2);

        // Then
        assertThat(response.getCurrentStep()).isEqualTo(3);
    }

    // Helper methods

    private OnboardingState createTestState() {
        OnboardingState state = new OnboardingState();
        state.setUserId(TEST_USER_ID);
        state.setCurrentStep(2);
        state.setTotalSteps(5);
        state.setIsCompleted(false);
        state.setIsTrialActive(false);
        return state;
    }

    private OnboardingState createActiveTrialState() {
        OnboardingState state = new OnboardingState();
        state.setUserId(TEST_USER_ID);
        state.setCurrentStep(5);
        state.setTotalSteps(5);
        state.setIsCompleted(true);
        state.setIsTrialActive(true);
        state.setTrialStartedAt(LocalDateTime.now());
        state.setTrialExpiredAt(LocalDateTime.now().plusDays(7));
        return state;
    }

    private OnboardingState createExpiredTrialState() {
        OnboardingState state = new OnboardingState();
        state.setUserId(TEST_USER_ID);
        state.setCurrentStep(5);
        state.setTotalSteps(5);
        state.setIsCompleted(true);
        state.setIsTrialActive(true);
        state.setTrialStartedAt(LocalDateTime.now().minusDays(10));
        state.setTrialExpiredAt(LocalDateTime.now().minusDays(3));
        return state;
    }
}
