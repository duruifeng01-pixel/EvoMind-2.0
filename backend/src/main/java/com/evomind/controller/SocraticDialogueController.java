package com.evomind.controller;

import com.evomind.dto.request.SendMessageRequest;
import com.evomind.dto.request.StartSocraticRequest;
import com.evomind.dto.response.ApiResponse;
import com.evomind.dto.response.SocraticDialogueResponse;
import com.evomind.dto.response.SocraticInsightResponse;
import com.evomind.dto.response.SocraticMessageResponse;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.SocraticDialogueService;
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

import java.util.List;

/**
 * 苏格拉底式对话控制器
 * AI通过追问引导用户深度思考
 */
@RestController
@RequestMapping("/api/socratic")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "苏格拉底式对话", description = "AI苏格拉底式对话引导深度思考")
public class SocraticDialogueController {

    private final SocraticDialogueService socraticDialogueService;

    @PostMapping("/start")
    @Operation(summary = "开始苏格拉底式对话", description = "基于讨论主题开始AI引导的深度对话")
    public ApiResponse<SocraticDialogueResponse> startDialogue(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody StartSocraticRequest request) {
        log.info("用户 {} 开始苏格拉底式对话", userDetails.getId());
        SocraticDialogueResponse response = socraticDialogueService.startDialogue(userDetails.getId(), request);
        return ApiResponse.success("对话已开始", response);
    }

    @PostMapping("/message")
    @Operation(summary = "发送消息", description = "发送用户回复并获取AI追问")
    public ApiResponse<SocraticMessageResponse> sendMessage(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody SendMessageRequest request) {
        SocraticMessageResponse response = socraticDialogueService.sendMessage(userDetails.getId(), request);
        return ApiResponse.success("回复成功", response);
    }

    @GetMapping("/dialogues/{id}")
    @Operation(summary = "获取对话详情", description = "获取对话会话详情")
    public ApiResponse<SocraticDialogueResponse> getDialogue(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "对话ID", required = true) @PathVariable Long id) {
        SocraticDialogueResponse response = socraticDialogueService.getDialogue(id, userDetails.getId());
        return ApiResponse.success("获取成功", response);
    }

    @GetMapping("/dialogues/{id}/messages")
    @Operation(summary = "获取对话消息", description = "获取对话的所有消息列表")
    public ApiResponse<List<SocraticMessageResponse>> getDialogueMessages(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "对话ID", required = true) @PathVariable Long id) {
        List<SocraticMessageResponse> messages = socraticDialogueService.getDialogueMessages(id, userDetails.getId());
        return ApiResponse.success("获取成功", messages);
    }

    @GetMapping("/dialogues")
    @Operation(summary = "获取用户对话列表", description = "分页获取用户的苏格拉底式对话历史")
    public ApiResponse<Page<SocraticDialogueResponse>> getUserDialogues(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SocraticDialogueResponse> dialogues = socraticDialogueService.getUserDialogues(userDetails.getId(), pageable);
        return ApiResponse.success("获取成功", dialogues);
    }

    @PostMapping("/dialogues/{id}/finalize")
    @Operation(summary = "结束对话并生成洞察", description = "结束对话并生成AI洞察总结")
    public ApiResponse<SocraticInsightResponse> finalizeDialogue(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "对话ID", required = true) @PathVariable Long id,
            @Parameter(description = "满意度评分1-5", example = "4") @RequestParam(required = false) Integer satisfaction) {
        SocraticInsightResponse response = socraticDialogueService.finalizeDialogue(
                userDetails.getId(), id, satisfaction);
        return ApiResponse.success("洞察生成成功", response);
    }

    @PostMapping("/dialogues/{id}/abandon")
    @Operation(summary = "放弃对话", description = "放弃当前对话（不生成洞察）")
    public ApiResponse<Void> abandonDialogue(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "对话ID", required = true) @PathVariable Long id) {
        socraticDialogueService.abandonDialogue(userDetails.getId(), id);
        return ApiResponse.success("对话已放弃", null);
    }

    @GetMapping("/discussions/{discussionId}/active")
    @Operation(summary = "获取活动对话", description = "获取用户在某个讨论下的活动对话")
    public ApiResponse<SocraticDialogueResponse> getActiveDialogue(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "讨论ID", required = true) @PathVariable Long discussionId) {
        SocraticDialogueResponse response = socraticDialogueService.getActiveDialogue(
                userDetails.getId(), discussionId);
        if (response == null) {
            return ApiResponse.success("没有进行中的对话", null);
        }
        return ApiResponse.success("获取成功", response);
    }

    @GetMapping("/discussions/{discussionId}/can-start")
    @Operation(summary = "检查是否可以开始对话", description = "检查用户是否可以在某讨论下开始新对话")
    public ApiResponse<Boolean> canStartDialogue(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "讨论ID", required = true) @PathVariable Long discussionId) {
        boolean canStart = socraticDialogueService.canStartDialogue(userDetails.getId(), discussionId);
        return ApiResponse.success("检查成功", canStart);
    }

    @PostMapping("/messages/{messageId}/regenerate")
    @Operation(summary = "重新生成AI回复", description = "对某条AI回复不满意时重新生成")
    public ApiResponse<SocraticMessageResponse> regenerateResponse(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "消息ID", required = true) @PathVariable Long messageId) {
        SocraticMessageResponse response = socraticDialogueService.regenerateResponse(
                userDetails.getId(), messageId);
        return ApiResponse.success("重新生成成功", response);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取对话统计", description = "获取用户的苏格拉底式对话统计数据")
    public ApiResponse<SocraticDialogueService.DialogueStats> getDialogueStats(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        SocraticDialogueService.DialogueStats stats = socraticDialogueService.getDialogueStats(userDetails.getId());
        return ApiResponse.success("获取成功", stats);
    }

    @PostMapping("/dialogues/{id}/save-as-card")
    @Operation(summary = "保存洞察为认知卡片", description = "将对话洞察保存到用户语料库")
    public ApiResponse<Long> saveInsightAsCard(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "对话ID", required = true) @PathVariable Long id) {
        Long cardId = socraticDialogueService.saveInsightAsCard(userDetails.getId(), id);
        return ApiResponse.success("已保存为认知卡片", cardId);
    }
}
