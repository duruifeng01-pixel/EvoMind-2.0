package com.evomind.service;

import com.evomind.dto.request.CreateCommentRequest;
import com.evomind.dto.request.FinalizeDiscussionRequest;
import com.evomind.dto.response.CommentResponse;
import com.evomind.dto.response.DiscussionInsightResponse;
import com.evomind.dto.response.DiscussionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 讨论服务接口
 */
public interface DiscussionService {

    /**
     * 获取今日讨论
     */
    DiscussionResponse getTodayDiscussion(Long currentUserId);

    /**
     * 获取讨论详情
     */
    DiscussionResponse getDiscussion(Long discussionId, Long currentUserId);

    /**
     * 获取讨论列表
     */
    Page<DiscussionResponse> getDiscussions(Pageable pageable, Long currentUserId);

    /**
     * 按标签查询讨论
     */
    Page<DiscussionResponse> getDiscussionsByTag(String tag, Pageable pageable, Long currentUserId);

    /**
     * 搜索讨论
     */
    Page<DiscussionResponse> searchDiscussions(String keyword, Pageable pageable, Long currentUserId);

    /**
     * 获取讨论的评论列表
     */
    Page<CommentResponse> getComments(Long discussionId, Pageable pageable, Long currentUserId);

    /**
     * 发表评论
     */
    CommentResponse createComment(Long userId, CreateCommentRequest request);

    /**
     * 删除评论
     */
    void deleteComment(Long userId, Long commentId);

    /**
     * 点赞评论
     */
    void likeComment(Long userId, Long commentId);

    /**
     * 取消点赞
     */
    void unlikeComment(Long userId, Long commentId);

    /**
     * 结束讨论并生成洞察
     */
    DiscussionInsightResponse finalizeDiscussion(Long userId, Long discussionId, FinalizeDiscussionRequest request);
}
