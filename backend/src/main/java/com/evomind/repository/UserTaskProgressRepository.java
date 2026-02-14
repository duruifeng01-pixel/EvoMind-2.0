package com.evomind.repository;

import com.evomind.entity.UserTaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTaskProgressRepository extends JpaRepository<UserTaskProgress, Long> {

    Optional<UserTaskProgress> findByUserIdAndTaskId(Long userId, Long taskId);

    Optional<UserTaskProgress> findByUserIdAndTaskIdAndDateKey(Long userId, Long taskId, String dateKey);

    List<UserTaskProgress> findByUserIdAndDateKey(Long userId, String dateKey);

    @Query("SELECT p FROM UserTaskProgress p WHERE p.userId = :userId AND p.isCompleted = true AND p.rewardClaimed = false")
    List<UserTaskProgress> findUnclaimedRewards(@Param("userId") Long userId);

    Long countByUserIdAndIsCompletedTrue(Long userId);
}
