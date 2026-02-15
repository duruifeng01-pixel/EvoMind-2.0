package com.evomind.repository;

import com.evomind.entity.SocraticDialogue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 苏格拉底式对话会话数据访问层
 */
@Repository
public interface SocraticDialogueRepository extends JpaRepository<SocraticDialogue, Long> {

    /**
     * 查找用户与某讨论的活动对话
     */
    Optional<SocraticDialogue> findByUserIdAndDiscussionIdAndStatus(
            Long userId, Long discussionId, SocraticDialogue.DialogueStatus status);

    /**
     * 查找用户在某个讨论下的所有对话（包括历史）
     */
    List<SocraticDialogue> findByUserIdAndDiscussionIdOrderByCreatedAtDesc(Long userId, Long discussionId);

    /**
     * 查找用户的所有活动对话
     */
    List<SocraticDialogue> findByUserIdAndStatus(Long userId, SocraticDialogue.DialogueStatus status);

    /**
     * 查找用户的所有对话（分页）
     */
    Page<SocraticDialogue> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 查找用户在某讨论下的最新对话
     */
    Optional<SocraticDialogue> findFirstByUserIdAndDiscussionIdOrderByCreatedAtDesc(
            Long userId, Long discussionId);

    /**
     * 检查用户在某讨论下是否有活动对话
     */
    boolean existsByUserIdAndDiscussionIdAndStatus(
            Long userId, Long discussionId, SocraticDialogue.DialogueStatus status);

    /**
     * 查找过期的进行中对话（超过24小时无响应）
     */
    @Query("SELECT d FROM SocraticDialogue d WHERE d.status = 'IN_PROGRESS' AND d.lastMessageAt < :timeout")
    List<SocraticDialogue> findExpiredDialogues(@Param("timeout") LocalDateTime timeout);

    /**
     * 查找需要生成洞察的对话（已完成但未生成洞察）
     */
    @Query("SELECT d FROM SocraticDialogue d WHERE d.status = 'COMPLETED' AND d.finalInsight IS NULL")
    List<SocraticDialogue> findPendingInsightGeneration();

    /**
     * 统计用户今日对话数量
     */
    @Query("SELECT COUNT(d) FROM SocraticDialogue d WHERE d.userId = :userId AND DATE(d.createdAt) = CURRENT_DATE")
    Long countTodayByUserId(@Param("userId") Long userId);

    /**
     * 统计用户在某讨论下的对话数量
     */
    Long countByUserIdAndDiscussionId(Long userId, Long discussionId);

    /**
     * 查找最近N个完成对话的用户ID（用于洞察生成）
     */
    @Query("SELECT DISTINCT d.userId FROM SocraticDialogue d WHERE d.status = 'COMPLETED' ORDER BY d.completedAt DESC")
    List<Long> findRecentCompletedUserIds(Pageable pageable);

    /**
     * 软删除对话（设置放弃状态）
     */
    @Modifying
    @Query("UPDATE SocraticDialogue d SET d.status = 'ABANDONED', d.isAbandoned = true, d.abandonedAt = CURRENT_TIMESTAMP " +
           "WHERE d.id = :id AND d.userId = :userId")
    int softDeleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
