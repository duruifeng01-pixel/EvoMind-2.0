package com.evomind.repository;

import com.evomind.entity.ContentModerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 内容审核日志数据访问层
 */
@Repository
public interface ContentModerationLogRepository extends JpaRepository<ContentModerationLog, Long> {

    /**
     * 根据内容ID和类型查询审核记录
     */
    Optional<ContentModerationLog> findByContentIdAndContentType(String contentId, 
                                                                  ContentModerationLog.ContentType contentType);

    /**
     * 根据内容ID查询最新审核记录
     */
    @Query("SELECT c FROM ContentModerationLog c WHERE c.contentId = :contentId ORDER BY c.createdAt DESC")
    List<ContentModerationLog> findByContentIdOrderByCreatedAtDesc(@Param("contentId") String contentId);

    /**
     * 分页查询审核日志
     */
    Page<ContentModerationLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据审核状态查询
     */
    Page<ContentModerationLog> findByModerationStatusOrderByCreatedAtDesc(
            ContentModerationLog.ModerationStatus status, Pageable pageable);

    /**
     * 查询需要人工复核的记录
     */
    Page<ContentModerationLog> findByManualReviewRequiredTrueAndModerationStatusOrderByCreatedAtDesc(
            ContentModerationLog.ModerationStatus status, Pageable pageable);

    /**
     * 查询AI生成内容的审核记录
     */
    Page<ContentModerationLog> findByIsAiGeneratedTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 统计指定时间范围内的审核结果
     */
    @Query("SELECT c.moderationStatus, COUNT(c) FROM ContentModerationLog c " +
           "WHERE c.createdAt BETWEEN :start AND :end GROUP BY c.moderationStatus")
    List<Object[]> countByStatusBetween(@Param("start") LocalDateTime start, 
                                        @Param("end") LocalDateTime end);

    /**
     * 统计用户的审核通过率和拒绝率
     */
    @Query("SELECT c.moderationStatus, COUNT(c) FROM ContentModerationLog c " +
           "WHERE c.userId = :userId GROUP BY c.moderationStatus")
    List<Object[]> countByUserIdGroupByStatus(@Param("userId") Long userId);

    /**
     * 查询最近的审核记录（用于监控）
     */
    List<ContentModerationLog> findTop50ByOrderByCreatedAtDesc();

    /**
     * 查询指定时间范围内创建的审核记录
     */
    Page<ContentModerationLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * 根据违规类型统计
     */
    @Query("SELECT c.violationType, COUNT(c) FROM ContentModerationLog c " +
           "WHERE c.violationType IS NOT NULL GROUP BY c.violationType ORDER BY COUNT(c) DESC")
    List<Object[]> countByViolationType();

    /**
     * 查询审核中的记录（用于清理异常状态）
     */
    @Query("SELECT c FROM ContentModerationLog c WHERE c.moderationStatus = 'PROCESSING' " +
           "AND c.createdAt < :before")
    List<ContentModerationLog> findProcessingBefore(@Param("before") LocalDateTime before);

    /**
     * 检查内容是否已经审核通过
     */
    boolean existsByContentIdAndContentTypeAndModerationStatus(
            String contentId, 
            ContentModerationLog.ContentType contentType,
            ContentModerationLog.ModerationStatus status);

    /**
     * 删除指定时间之前的日志（数据清理）
     */
    void deleteByCreatedAtBefore(LocalDateTime before);
}