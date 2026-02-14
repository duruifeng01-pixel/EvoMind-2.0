package com.evomind.service;

import com.evomind.dto.request.SubmitArtifactRequest;
import com.evomind.dto.response.ChallengeTaskResponse;

import java.util.List;

/**
 * 挑战任务服务接口
 */
public interface ChallengeTaskService {

    /**
     * 获取今日挑战任务
     */
    ChallengeTaskResponse getTodayTask(Long userId);

    /**
     * 获取用户任务进度
     */
    ChallengeTaskResponse getTaskProgress(Long userId, Long taskId);

    /**
     * 更新任务进度
     */
    ChallengeTaskResponse updateProgress(Long userId, Long taskId, int increment);

    /**
     * 领取任务奖励
     */
    ChallengeTaskResponse claimReward(Long userId, Long taskId);

    /**
     * 获取用户未领取的奖励列表
     */
    List<ChallengeTaskResponse> getUnclaimedRewards(Long userId);

    /**
     * 记录用户行为（用于自动更新任务进度）
     */
    void recordUserActivity(Long userId, ActivityType activityType);

    /**
     * 提交作品
     */
    ChallengeTaskResponse submitArtifact(Long userId, Long taskId, SubmitArtifactRequest request);

    /**
     * 用户活动类型
     */
    enum ActivityType {
        READ_CARD,
        ADD_SOURCE,
        CREATE_NOTE,
        JOIN_DISCUSSION,
        SHARE_INSIGHT,
        DAILY_LOGIN
    }
}
