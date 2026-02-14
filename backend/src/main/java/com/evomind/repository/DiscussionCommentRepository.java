package com.evomind.repository;

import com.evomind.entity.DiscussionComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 讨论评论Repository
 */
@Repository
public interface DiscussionCommentRepository extends JpaRepository<DiscussionComment, Long> {

    /**
     * 查询讨论的一级评论
     */
    Page<DiscussionComment> findByDiscussionIdAndParentIdIsNullAndIsDeletedFalseOrderByIsTopDescCreatedAtDesc(
            Long discussionId, Pageable pageable);

    /**
     * 查询评论的回复
     */
    List<DiscussionComment> findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(Long parentId);

    /**
     * 查询讨论的所有评论（包括回复）
     */
    @Query("SELECT c FROM DiscussionComment c WHERE c.discussionId = :discussionId AND c.isDeleted = false ORDER BY c.parentId ASC, c.createdAt ASC")
    List<DiscussionComment> findAllByDiscussionId(@Param("discussionId") Long discussionId);

    /**
     * 统计讨论的评论数
     */
    Long countByDiscussionIdAndIsDeletedFalse(Long discussionId);

    /**
     * 查询用户的评论
     */
    Page<DiscussionComment> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 查询用户是否参与过某讨论
     */
    boolean existsByDiscussionIdAndUserId(Long discussionId, Long userId);
}
