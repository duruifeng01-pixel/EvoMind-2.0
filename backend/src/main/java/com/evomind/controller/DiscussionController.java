package com.evomind.controller;

import com.evomind.dto.request.CreateCommentRequest;
import com.evomind.dto.response.CommentResponse;
import com.evomind.dto.response.DiscussionResponse;
import com.evomind.dto.response.Result;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.DiscussionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 讨论控制器
 * 每日一问/讨论系统API
 */
@Slf4j
@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
@Tag(name = "讨论管理", description = "每日讨论主题、评论相关接口")
public class DiscussionController {

    private final DiscussionService discussionService;

    @Operation(summary = "获取今日讨论", description = "获取当天的讨论主题")
    @GetMapping("/today")
    public Result<DiscussionResponse> getTodayDiscussion(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        DiscussionResponse response = discussionService.getTodayDiscussion(userId);
        return Result.success(response);
    }

    @Operation(summary = "获取讨论详情", description = "根据ID获取讨论详情")
    @GetMapping("/{id}")
    public Result<DiscussionResponse> getDiscussion(
            @Parameter(description = "讨论ID", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        DiscussionResponse response = discussionService.getDiscussion(id, userId);
        return Result.success(response);
    }

    @Operation(summary = "获取讨论列表", description = "分页获取已发布的讨论列表")
    @GetMapping
    public Result<Page<DiscussionResponse>> getDiscussions(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateKey"));
        Page<DiscussionResponse> discussions = discussionService.getDiscussions(pageable, userId);
        return Result.success(discussions);
    }

    @Operation(summary = "按标签查询讨论", description = "根据话题标签筛选讨论")
    @GetMapping("/tag/{tag}")
    public Result<Page<DiscussionResponse>> getDiscussionsByTag(
            @Parameter(description = "话题标签", required = true) @PathVariable String tag,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateKey"));
        Page<DiscussionResponse> discussions = discussionService.getDiscussionsByTag(tag, pageable, userId);
        return Result.success(discussions);
    }

    @Operation(summary = "搜索讨论", description = "根据关键词搜索讨论标题和内容")
    @GetMapping("/search")
    public Result<Page<DiscussionResponse>> searchDiscussions(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateKey"));
        Page<DiscussionResponse> discussions = discussionService.searchDiscussions(keyword, pageable, userId);
        return Result.success(discussions);
    }

    @Operation(summary = "获取评论列表", description = "获取讨论的评论列表，包含一级评论和回复")
    @GetMapping("/{discussionId}/comments")
    public Result<Page<CommentResponse>> getComments(
            @Parameter(description = "讨论ID", required = true) @PathVariable Long discussionId,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isTop", "createdAt"));
        Page<CommentResponse> comments = discussionService.getComments(discussionId, pageable, userId);
        return Result.success(comments);
    }

    @Operation(summary = "发表评论", description = "在讨论下发表评论或回复其他评论")
    @PostMapping("/comments")
    public Result<CommentResponse> createComment(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = discussionService.createComment(userDetails.getId(), request);
        return Result.success(response);
    }

    @Operation(summary = "删除评论", description = "删除自己发表的评论（软删除）")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        discussionService.deleteComment(userDetails.getId(), commentId);
        return Result.success();
    }

    @Operation(summary = "点赞评论", description = "给评论点赞")
    @PostMapping("/comments/{commentId}/like")
    public Result<Void> likeComment(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        discussionService.likeComment(userDetails.getId(), commentId);
        return Result.success();
    }

    @Operation(summary = "取消点赞", description = "取消对评论的点赞")
    @DeleteMapping("/comments/{commentId}/like")
    public Result<Void> unlikeComment(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        discussionService.unlikeComment(userDetails.getId(), commentId);
        return Result.success();
    }
}
