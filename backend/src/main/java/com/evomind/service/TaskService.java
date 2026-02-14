package com.evomind.service;

import com.evomind.entity.Task;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {

    Task createTask(Long userId, String title, String description, String stage, String difficulty, 
                    LocalDateTime deadline, Integer rewardPoints, Long relatedCardId);

    Task getTaskById(Long id, Long userId);

    List<Task> getTasksByUserId(Long userId);

    List<Task> getTasksByStatus(Long userId, String status);

    List<Task> getTasksByStage(Long userId, String stage);

    List<Task> getPendingTasks(Long userId);

    List<Task> getOverdueTasks(Long userId);

    List<Task> getTasksByDeadlineRange(Long userId, LocalDateTime start, LocalDateTime end);

    Task updateTask(Long id, Long userId, String title, String description, String difficulty, LocalDateTime deadline);

    void deleteTask(Long id, Long userId);

    void completeTask(Long id, Long userId);

    long countByUserId(Long userId);

    long countByStatus(Long userId, String status);

    int getTotalRewardPoints(Long userId);
}
