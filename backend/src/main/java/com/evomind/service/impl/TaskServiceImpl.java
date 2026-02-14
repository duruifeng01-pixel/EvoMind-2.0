package com.evomind.service.impl;

import com.evomind.entity.Task;
import com.evomind.exception.BusinessException;
import com.evomind.repository.TaskRepository;
import com.evomind.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public Task createTask(Long userId, String title, String description, String stage, 
                          String difficulty, LocalDateTime deadline, Integer rewardPoints, Long relatedCardId) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(title);
        task.setDescription(description);
        task.setStage(stage);
        task.setDifficulty(difficulty);
        task.setDeadlineAt(deadline);
        task.setRewardPoints(rewardPoints != null ? rewardPoints : 0);
        task.setRelatedCardId(relatedCardId);
        task.setStatus("PENDING");
        task.setReminderSent(false);
        return taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskById(Long id, Long userId) {
        return taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException("任务不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByUserId(Long userId) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(Long userId, String status) {
        return taskRepository.findByUserIdAndStatusOrderByDeadlineAtAsc(userId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByStage(Long userId, String stage) {
        return taskRepository.findByUserIdAndStageOrderByCreatedAtDesc(userId, stage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getPendingTasks(Long userId) {
        return taskRepository.findByUserIdAndStatusOrderByDeadlineAtAsc(userId, "PENDING");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getOverdueTasks(Long userId) {
        return taskRepository.findOverdueTasks(userId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByDeadlineRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return taskRepository.findTasksByDeadlineRange(userId, start, end);
    }

    @Override
    @Transactional
    public Task updateTask(Long id, Long userId, String title, String description, 
                          String difficulty, LocalDateTime deadline) {
        Task task = getTaskById(id, userId);
        if (title != null) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (difficulty != null) {
            task.setDifficulty(difficulty);
        }
        if (deadline != null) {
            task.setDeadlineAt(deadline);
        }
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long id, Long userId) {
        Task task = getTaskById(id, userId);
        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public void completeTask(Long id, Long userId) {
        Task task = getTaskById(id, userId);
        if ("COMPLETED".equals(task.getStatus())) {
            throw new BusinessException("任务已完成");
        }
        taskRepository.markAsCompleted(id, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(Long userId) {
        return taskRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Long userId, String status) {
        return taskRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalRewardPoints(Long userId) {
        Integer points = taskRepository.sumCompletedTaskPoints(userId);
        return points != null ? points : 0;
    }
}
