package com.evomind.service.impl;

import com.evomind.dto.request.CreateCommentRequest;
import com.evomind.dto.request.FinalizeDiscussionRequest;
import com.evomind.dto.response.CommentResponse;
import com.evomind.dto.response.DiscussionInsightResponse;
import com.evomind.dto.response.DiscussionResponse;
import com.evomind.entity.Discussion;
import com.evomind.entity.DiscussionComment;
import com.evomind.entity.User;
import com.evomind.exception.BusinessException;
import com.evomind.exception.ResourceNotFoundException;
import com.evomind.repository.DiscussionCommentRepository;
import com.evomind.repository.DiscussionRepository;
import com.evomind.repository.UserRepository;
import com.evomind.service.DiscussionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 讨论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscussionServiceImpl implements DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final DiscussionCommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DiscussionResponse getTodayDiscussion(Long currentUserId) {
        Discussion discussion = discussionRepository.findToday()
                .orElseThrow(() -> new ResourceNotFoundException("今日讨论尚未发布"));
        return convertToDiscussionResponse(discussion, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public DiscussionResponse getDiscussion(Long discussionId, Long currentUserId) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .filter(Discussion::getIsPublished)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在或未发布"));
        return convertToDiscussionResponse(discussion, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionResponse> getDiscussions(Pageable pageable, Long currentUserId) {
        Page<Discussion> discussions = discussionRepository.findByIsPublishedTrueOrderByDateKeyDesc(pageable);
        return discussions.map(d -> convertToDiscussionResponse(d, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionResponse> getDiscussionsByTag(String tag, Pageable pageable, Long currentUserId) {
        Page<Discussion> discussions = discussionRepository.findByTopicTagAndIsPublishedTrueOrderByDateKeyDesc(tag, pageable);
        return discussions.map(d -> convertToDiscussionResponse(d, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionResponse> searchDiscussions(String keyword, Pageable pageable, Long currentUserId) {
        Page<Discussion> discussions = discussionRepository.searchDiscussions(keyword, pageable);
        return discussions.map(d -> convertToDiscussionResponse(d, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long discussionId, Pageable pageable, Long currentUserId) {
        // 验证讨论存在
        if (!discussionRepository.existsById(discussionId)) {
            throw new ResourceNotFoundException("讨论不存在");
        }

        // 查询一级评论
        Page<DiscussionComment> topComments = commentRepository
                .findByDiscussionIdAndParentIdIsNullAndIsDeletedFalseOrderByIsTopDescCreatedAtDesc(discussionId, pageable);

        // 获取所有一级评论ID
        List<Long> topCommentIds = topComments.getContent().stream()
                .map(DiscussionComment::getId)
                .collect(Collectors.toList());

        // 批量查询回复
        List<DiscussionComment> allReplies = new ArrayList<>();
        if (!topCommentIds.isEmpty()) {
            for (Long parentId : topCommentIds) {
                allReplies.addAll(commentRepository.findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(parentId));
            }
        }

        // 按parentId分组回复
        Map<Long, List<DiscussionComment>> repliesMap = allReplies.stream()
                .collect(Collectors.groupingBy(DiscussionComment::getParentId));

        // 获取所有用户ID
        List<Long> userIds = topComments.getContent().stream()
                .map(DiscussionComment::getUserId)
                .collect(Collectors.toList());
        userIds.addAll(allReplies.stream().map(DiscussionComment::getUserId).collect(Collectors.toList()));
        userIds.addAll(allReplies.stream()
                .filter(r -> r.getReplyToUserId() != null)
                .map(DiscussionComment::getReplyToUserId)
                .collect(Collectors.toList()));

        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 转换响应
        List<CommentResponse> responses = topComments.getContent().stream()
                .map(c -> convertToCommentResponse(c, repliesMap.getOrDefault(c.getId(), new ArrayList<>()), userMap, currentUserId))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, topComments.getTotalElements());
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long userId, CreateCommentRequest request) {
        Discussion discussion = discussionRepository.findById(request.getDiscussionId())
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在"));

        // 创建评论
        DiscussionComment comment = new DiscussionComment();
        comment.setDiscussionId(request.getDiscussionId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setLikeCount(0);
        comment.setIsDeleted(false);

        DiscussionComment saved = commentRepository.save(comment);

        // 更新讨论评论数
        discussion.incrementComment();

        // 更新讨论参与人数（如果用户首次参与）
        boolean hasParticipated = commentRepository.existsByDiscussionIdAndUserId(request.getDiscussionId(), userId);
        if (!hasParticipated) {
            discussion.incrementParticipant();
        }

        discussionRepository.save(discussion);

        log.info("用户 {} 在讨论 {} 发表评论 {}", userId, request.getDiscussionId(), saved.getId());

        // 获取用户信息返回
        User user = userRepository.findById(userId).orElse(null);
        Map<Long, User> userMap = user != null ? Map.of(userId, user) : Map.of();

        return convertToCommentResponse(saved, new ArrayList<>(), userMap, userId);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        DiscussionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在"));

        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人评论");
        }

        comment.softDelete();
        commentRepository.save(comment);

        // 更新讨论评论数
        Discussion discussion = discussionRepository.findById(comment.getDiscussionId()).orElse(null);
        if (discussion != null && discussion.getCommentCount() > 0) {
            discussion.setCommentCount(discussion.getCommentCount() - 1);
            discussionRepository.save(discussion);
        }

        log.info("用户 {} 删除评论 {}", userId, commentId);
    }

    @Override
    @Transactional
    public void likeComment(Long userId, Long commentId) {
        // TODO: 实现点赞功能，需要添加评论点赞记录表
        DiscussionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在"));

        comment.incrementLike();
        commentRepository.save(comment);

        log.info("用户 {} 点赞评论 {}", userId, commentId);
    }

    @Override
    @Transactional
    public void unlikeComment(Long userId, Long commentId) {
        DiscussionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在"));

        comment.decrementLike();
        commentRepository.save(comment);

        log.info("用户 {} 取消点赞评论 {}", userId, commentId);
    }

    @Override
    @Transactional
    public DiscussionInsightResponse finalizeDiscussion(Long userId, Long discussionId, FinalizeDiscussionRequest request) {
        Discussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在"));

        // 获取用户在该讨论下的所有评论统计
        List<DiscussionComment> userComments = commentRepository.findByDiscussionIdAndUserId(discussionId, userId);
        long topLevelCount = userComments.stream().filter(c -> c.getParentId() == null).count();
        long replyCount = userComments.stream().filter(c -> c.getParentId() != null).count();
        int totalLikes = userComments.stream().mapToInt(c -> c.getLikeCount() != null ? c.getLikeCount() : 0).sum();

        // TODO: 调用AI服务生成洞察总结
        // 目前返回模拟数据
        DiscussionInsightResponse response = new DiscussionInsightResponse();
        response.setId(1L);
        response.setDiscussionId(discussionId);
        response.setUserId(userId);
        response.setAiSummary("基于讨论内容，AI生成的核心观点总结...");
        response.setKeyInsights(List.of(
                "AI技术正在重塑传统行业的工作模式",
                "人机协作将成为未来的主流工作方式",
                "持续学习是应对AI时代的关键能力"
        ));
        response.setPersonalInsight(request.getPersonalInsight());

        DiscussionInsightResponse.ParticipationStats stats = new DiscussionInsightResponse.ParticipationStats();
        stats.setTopLevelComments((int) topLevelCount);
        stats.setReplies((int) replyCount);
        stats.setTotalLikesReceived(totalLikes);
        stats.setTotalParticipants(discussion.getParticipantCount());
        response.setStats(stats);

        response.setCreatedAt(LocalDateTime.now());

        log.info("用户 {} 结束讨论 {}，生成洞察", userId, discussionId);
        return response;
    }

    /**
     * 转换为讨论响应
     */
    private DiscussionResponse convertToDiscussionResponse(Discussion discussion, Long currentUserId) {
        DiscussionResponse response = new DiscussionResponse();
        response.setId(discussion.getId());
        response.setDateKey(discussion.getDateKey());
        response.setTitle(discussion.getTitle());
        response.setContent(discussion.getContent());
        response.setTopicTag(discussion.getTopicTag());
        response.setParticipantCount(discussion.getParticipantCount());
        response.setCommentCount(discussion.getCommentCount());
        response.setPublishedAt(discussion.getPublishedAt());

        // 检查当前用户是否参与过
        if (currentUserId != null) {
            response.setHasParticipated(commentRepository.existsByDiscussionIdAndUserId(discussion.getId(), currentUserId));
        } else {
            response.setHasParticipated(false);
        }

        // TODO: 解析关联的卡片和信息源
        response.setRelatedCards(new ArrayList<>());
        response.setRelatedSources(new ArrayList<>());

        return response;
    }

    /**
     * 转换为评论响应
     */
    private CommentResponse convertToCommentResponse(DiscussionComment comment,
                                                      List<DiscussionComment> replies,
                                                      Map<Long, User> userMap,
                                                      Long currentUserId) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setDiscussionId(comment.getDiscussionId());
        response.setUserId(comment.getUserId());
        response.setContent(comment.getContent());
        response.setParentId(comment.getParentId());
        response.setReplyToUserId(comment.getReplyToUserId());
        response.setLikeCount(comment.getLikeCount());
        response.setIsTop(comment.getIsTop());
        response.setReplyCount(replies.size());
        response.setCreatedAt(comment.getCreatedAt());

        // 设置用户信息
        User user = userMap.get(comment.getUserId());
        if (user != null) {
            response.setUserNickname(user.getNickname());
            // TODO: response.setUserAvatar(user.getAvatar());
        }

        // 设置回复用户信息
        if (comment.getReplyToUserId() != null) {
            User replyToUser = userMap.get(comment.getReplyToUserId());
            if (replyToUser != null) {
                response.setReplyToUserNickname(replyToUser.getNickname());
            }
        }

        // 设置回复列表
        List<CommentResponse> replyResponses = replies.stream()
                .map(r -> {
                    CommentResponse rr = new CommentResponse();
                    rr.setId(r.getId());
                    rr.setDiscussionId(r.getDiscussionId());
                    rr.setUserId(r.getUserId());
                    rr.setContent(r.getContent());
                    rr.setParentId(r.getParentId());
                    rr.setReplyToUserId(r.getReplyToUserId());
                    rr.setLikeCount(r.getLikeCount());
                    rr.setCreatedAt(r.getCreatedAt());

                    User replyUser = userMap.get(r.getUserId());
                    if (replyUser != null) {
                        rr.setUserNickname(replyUser.getNickname());
                    }

                    if (r.getReplyToUserId() != null) {
                        User rtUser = userMap.get(r.getReplyToUserId());
                        if (rtUser != null) {
                            rr.setReplyToUserNickname(rtUser.getNickname());
                        }
                    }

                    rr.setHasLiked(false); // TODO: 实现点赞状态查询
                    return rr;
                })
                .collect(Collectors.toList());

        response.setReplies(replyResponses);
        response.setHasLiked(false); // TODO: 实现点赞状态查询

        return response;
    }
}
