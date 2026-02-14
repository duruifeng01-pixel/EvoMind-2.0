package com.evomind.service.impl;

import com.evomind.dto.request.SubmitArtifactRequest;
import com.evomind.dto.response.ChallengeTaskResponse;
import com.evomind.entity.ChallengeTask;
import com.evomind.entity.UserTaskProgress;
import com.evomind.exception.BusinessException;
import com.evomind.exception.ResourceNotFoundException;
import com.evomind.repository.ChallengeTaskRepository;
import com.evomind.repository.UserTaskProgressRepository;
import com.evomind.service.ChallengeTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 挑战任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeTaskServiceImpl implements ChallengeTaskService {

    private final ChallengeTaskRepository taskRepository;
    private final UserTaskProgressRepository progressRepository;

    @Override
    @Transactional(readOnly = true)
    public ChallengeTaskResponse getTodayTask(Long userId) {
        ChallengeTask task = taskRepository.findToday()
                .filter(ChallengeTask::getIsPublished)
                .orElseThrow(() -> new ResourceNotFoundException("今日任务尚未发布"));

        return getTaskWithProgress(task, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ChallengeTaskResponse getTaskProgress(Long userId, Long taskId) {
        ChallengeTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        return getTaskWithProgress(task, userId);
    }

    @Override
    @Transactional
    public ChallengeTaskResponse updateProgress(Long userId, Long taskId, int increment) {
        ChallengeTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 查找或创建用户进度
        UserTaskProgress progress = progressRepository
                .findByUserIdAndTaskIdAndDateKey(userId, taskId, today)
                .orElseGet(() -> createNewProgress(userId, taskId, today));

        // 如果已完成则不更新
        if (progress.getIsCompleted()) {
            return getTaskWithProgress(task, progress);
        }

        // 更新进度
        progress.incrementProgress(increment, task.getTargetCount());
        progress.setUpdatedAt(LocalDateTime.now());
        progressRepository.save(progress);

        if (progress.getIsCompleted()) {
            log.info("用户 {} 完成任务 {}，获得 {} 积分奖励",
                    userId, taskId, task.getRewardPoints());
        }

        return getTaskWithProgress(task, progress);
    }

    @Override
    @Transactional
    public ChallengeTaskResponse claimReward(Long userId, Long taskId) {
        ChallengeTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        UserTaskProgress progress = progressRepository
                .findByUserIdAndTaskIdAndDateKey(userId, taskId, today)
                .orElseThrow(() -> new BusinessException("任务进度不存在"));

        if (!progress.getIsCompleted()) {
            throw new BusinessException("任务尚未完成");
        }

        progress.claimReward();
        progressRepository.save(progress);

        // TODO: 发放积分和体验天数奖励
        log.info("用户 {} 领取任务 {} 奖励：{} 积分 + {} 天体验",
                userId, taskId, task.getRewardPoints(), task.getRewardTrialDays());

        return getTaskWithProgress(task, progress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChallengeTaskResponse> getUnclaimedRewards(Long userId) {
        List<UserTaskProgress> unclaimed = progressRepository.findUnclaimedRewards(userId);

        return unclaimed.stream()
                .map(p -> {
                    ChallengeTask task = taskRepository.findById(p.getTaskId()).orElse(null);
                    if (task == null) return null;
                    return getTaskWithProgress(task, p);
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recordUserActivity(Long userId, ActivityType activityType) {
        // 获取今日任务
        Optional<ChallengeTask> todayTaskOpt = taskRepository.findToday()
                .filter(ChallengeTask::getIsPublished);

        if (todayTaskOpt.isEmpty()) {
            return;
        }

        ChallengeTask task = todayTaskOpt.get();

        // 检查活动类型是否匹配任务类型
        boolean shouldUpdate = switch (task.getTaskType()) {
            case READ_CARDS -> activityType == ActivityType.READ_CARD;
            case ADD_SOURCES -> activityType == ActivityType.ADD_SOURCE;
            case CREATE_NOTES -> activityType == ActivityType.CREATE_NOTE;
            case COMPLETE_DISCUSSION -> activityType == ActivityType.JOIN_DISCUSSION;
            case SHARE_INSIGHT -> activityType == ActivityType.SHARE_INSIGHT;
            case DAILY_CHECKIN -> activityType == ActivityType.DAILY_LOGIN;
        };

        if (shouldUpdate) {
            updateProgress(userId, task.getId(), 1);
            log.debug("用户 {} 的 {} 活动已记录到今日任务", userId, activityType);
        }
    }

    /**
     * 创建新的进度记录
     */
    private UserTaskProgress createNewProgress(Long userId, Long taskId, String dateKey) {
        UserTaskProgress progress = new UserTaskProgress();
        progress.setUserId(userId);
        progress.setTaskId(taskId);
        progress.setDateKey(dateKey);
        progress.setCurrentCount(0);
        progress.setIsCompleted(false);
        progress.setRewardClaimed(false);
        progress.setCreatedAt(LocalDateTime.now());
        progress.setUpdatedAt(LocalDateTime.now());
        return progressRepository.save(progress);
    }

    /**
     * 获取带进度的任务响应
     */
    private ChallengeTaskResponse getTaskWithProgress(ChallengeTask task, Long userId) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        UserTaskProgress progress = progressRepository
                .findByUserIdAndTaskIdAndDateKey(userId, task.getId(), today)
                .orElse(null);
        return getTaskWithProgress(task, progress);
    }

    /**
     * 转换为响应DTO
     */
    private ChallengeTaskResponse getTaskWithProgress(ChallengeTask task, UserTaskProgress progress) {
        ChallengeTaskResponse response = new ChallengeTaskResponse();
        response.setId(task.getId());
        response.setDateKey(task.getDateKey());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setTaskType(task.getTaskType().name());
        response.setTargetCount(task.getTargetCount());
        response.setRewardPoints(task.getRewardPoints());
        response.setRewardTrialDays(task.getRewardTrialDays());
        response.setIsToday(task.isToday());

        // 设置用户进度
        if (progress != null) {
            response.setCurrentCount(progress.getCurrentCount());
            response.setProgressPercent(progress.getProgressPercent(task.getTargetCount()));
            response.setIsCompleted(progress.getIsCompleted());
            response.setRewardClaimed(progress.getRewardClaimed());
            response.setCompletedAt(progress.getCompletedAt());
        } else {
            response.setCurrentCount(0);
            response.setProgressPercent(0);
            response.setIsCompleted(false);
            response.setRewardClaimed(false);
        }

        return response;
    }

    @Override
    @Transactional
    public ChallengeTaskResponse submitArtifact(Long userId, Long taskId, SubmitArtifactRequest request) {
        ChallengeTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 查找或创建用户进度
        UserTaskProgress progress = progressRepository
                .findByUserIdAndTaskIdAndDateKey(userId, taskId, today)
                .orElseGet(() -> createNewProgress(userId, taskId, today));

        // 如果已完成则不更新
        if (!progress.getIsCompleted()) {
            // 提交作品视为完成一次任务
            progress.incrementProgress(1, task.getTargetCount());
            progress.setUpdatedAt(LocalDateTime.now());
            progressRepository.save(progress);

            log.info("用户 {} 提交任务 {} 作品: {}", userId, taskId, request.getTitle());
        }

        // TODO: 保存作品信息到作品表

        return getTaskWithProgress(task, progress);
    }
}
