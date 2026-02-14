package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import com.evomind.dto.request.CreateCommentRequest;
import com.evomind.dto.request.FinalizeDiscussionRequest;
import com.evomind.dto.response.CommentResponse;
import com.evomind.dto.response.DiscussionResponse;
import com.evomind.dto.response.DiscussionInsightResponse;
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
 * 每日一问/讨论系统API - 符合API契约 v1
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/discussion")
@RequiredArgsConstructor
@Tag(name = "讨论管理", description = "每日讨论主题、评论相关接口")
public class DiscussionController {

    private final DiscussionService discussionService;

    @Operation(summary = "生成/获取每日问题", description = "获取当天的讨论主题（自动生成或返回已有）")
    @PostMapping("/daily-question/generate")
    public ApiResponse<DiscussionResponse> generateDailyQuestion(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        DiscussionResponse response = discussionService.getTodayDiscussion(userId);
        return ApiResponse.success("获取成功", response);
    }

    @Operation(summary = "获取讨论详情", description = "根据ID获取讨论详情")
    @GetMapping("/{id}")
    public ApiResponse<DiscussionResponse> getDiscussion(
            @Parameter(description = "讨论ID", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        DiscussionResponse response = discussionService.getDiscussion(id, userId);
        return ApiResponse.success("获取成功", response);
    }

    @Operation(summary = "获取讨论列表", description = "分页获取已发布的讨论列表")
    @GetMapping
    public ApiResponse<Page<DiscussionResponse>> getDiscussions(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateKey"));
        Page<DiscussionResponse> discussions = discussionService.getDiscussions(pageable, userId);
        return ApiResponse.success("获取成功", discussions);
    }

    @Operation(summary = "回复讨论", description = "在讨论下发表评论或回复其他评论")
    @PostMapping("/{id}/reply")
    public ApiResponse<CommentResponse> replyDiscussion(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "讨论ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request) {
        // 确保discussionId与路径参数一致
        request.setDiscussionId(id);
        CommentResponse response = discussionService.createComment(userDetails.getId(), request);
        return ApiResponse.success("发表成功", response);
    }

    @Operation(summary = "结束讨论并生成洞察", description = "结束讨论，生成AI洞察总结")
    @PostMapping("/{id}/finalize")
    public ApiResponse<DiscussionInsightResponse> finalizeDiscussion(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "讨论ID", required = true) @PathVariable Long id,
            @Valid @RequestBody FinalizeDiscussionRequest request) {
        DiscussionInsightResponse response = discussionService.finalizeDiscussion(
                userDetails.getId(), id, request);
        return ApiResponse.success("讨论已结束，洞察生成成功", response);
    }

    @Operation(summary = "获取评论列表", description = "获取讨论的评论列表，包含一级评论和回复")
    @GetMapping("/{id}/comments")
    public ApiResponse<Page<CommentResponse>> getComments(
            @Parameter(description = "讨论ID", required = true) @PathVariable Long id,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails != null ? userDetails.getId() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isTop", "createdAt"));
        Page<CommentResponse> comments = discussionService.getComments(id, pageable, userId);
        return ApiResponse.success("获取成功", comments);
    }

    @Operation(summary = "删除评论", description = "删除自己发表的评论（软删除）")
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        discussionService.deleteComment(userDetails.getId(), commentId);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "点赞评论", description = "给评论点赞")
    @PostMapping("/comments/{commentId}/like")
    public ApiResponse<Void> likeComment(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        discussionService.likeComment(userDetails.getId(), commentId);
        return ApiResponse.success("点赞成功", null);
    }

    @Operation(summary = "取消点赞", description = "取消对评论的点赞")
    @DeleteMapping("/comments/{commentId}/like")
    public ApiResponse<Void> unlikeComment(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long commentId) {
        discussionService.unlikeComment(userDetails.getId(), commentId);
        return ApiResponse.success("取消点赞成功", null);
    }
}
