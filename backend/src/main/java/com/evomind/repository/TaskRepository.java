package com.evomind.repository;

import com.evomind.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Task> findByUserIdAndStatusOrderByDeadlineAtAsc(Long userId, String status);

    List<Task> findByUserIdAndStageOrderByCreatedAtDesc(Long userId, String stage);

    List<Task> findByUserIdAndStatusInOrderByDeadlineAtAsc(Long userId, List<String> statuses);

    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.status != 'COMPLETED' " +
           "AND t.deadlineAt < :now ORDER BY t.deadlineAt ASC")
    List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.status != 'COMPLETED' " +
           "AND t.deadlineAt BETWEEN :start AND :end ORDER BY t.deadlineAt ASC")
    List<Task> findTasksByDeadlineRange(@Param("userId") Long userId, 
                                         @Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end);

    @Modifying
    @Query("UPDATE Task t SET t.status = 'COMPLETED', t.completedAt = :now WHERE t.id = :id")
    void markAsCompleted(@Param("id") Long id, @Param("now") LocalDateTime now);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    @Query("SELECT COALESCE(SUM(t.rewardPoints), 0) FROM Task t WHERE t.userId = :userId AND t.status = 'COMPLETED'")
    Integer sumCompletedTaskPoints(@Param("userId") Long userId);
}
