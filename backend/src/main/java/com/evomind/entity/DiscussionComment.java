package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 讨论评论实体
 */
@Entity
@Table(name = "discussion_comments")
@Getter
@Setter
public class DiscussionComment extends BaseEntity {

    @Column(name = "discussion_id", nullable = false)
    private Long discussionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "parent_id")
    private Long parentId; // 回复的评论ID，一级评论为null

    @Column(name = "reply_to_user_id")
    private Long replyToUserId; // 回复给哪个用户

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "is_top")
    private Boolean isTop = false;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 增加点赞数
     */
    public void incrementLike() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }

    /**
     * 减少点赞数
     */
    public void decrementLike() {
        if (this.likeCount != null && this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 软删除
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 是否是一级评论
     */
    public boolean isTopLevel() {
        return this.parentId == null;
    }
}
