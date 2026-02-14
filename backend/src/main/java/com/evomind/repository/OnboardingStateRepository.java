package com.evomind.repository;

import com.evomind.entity.OnboardingState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 新手引导状态数据访问层
 */
@Repository
public interface OnboardingStateRepository extends JpaRepository<OnboardingState, Long> {

    /**
     * 根据用户ID查询引导状态
     */
    Optional<OnboardingState> findByUserId(Long userId);

    /**
     * 检查用户是否有引导状态记录
     */
    boolean existsByUserId(Long userId);

    /**
     * 查询已完成引导的用户
     */
    List<OnboardingState> findByIsCompletedTrue();

    /**
     * 查询未完成引导的用户
     */
    List<OnboardingState> findByIsCompletedFalse();

    /**
     * 查询处于特定步骤的用户
     */
    List<OnboardingState> findByCurrentStep(Integer currentStep);

    /**
     * 查询试用期即将过期的用户
     */
    @Query("SELECT o FROM OnboardingState o WHERE o.isTrialActive = true AND o.trialExpiredAt BETWEEN :now AND :expireTime")
    List<OnboardingState> findExpiringTrials(@Param("now") LocalDateTime now, @Param("expireTime") LocalDateTime expireTime);

    /**
     * 查询试用期已过期的用户
     */
    @Query("SELECT o FROM OnboardingState o WHERE o.isTrialActive = true AND o.trialExpiredAt < :now")
    List<OnboardingState> findExpiredTrials(@Param("now") LocalDateTime now);

    /**
     * 统计已完成引导的用户数
     */
    long countByIsCompletedTrue();

    /**
     * 统计未完成引导的用户数
     */
    long countByIsCompletedFalse();
}
