package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import com.evomind.dto.response.ConflictResponse;
import com.evomind.entity.Card;
import com.evomind.entity.CardConflict;
import com.evomind.repository.CardRepository;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.ConflictDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 观点冲突控制器
 * 认知冲突检测与管理API - 符合API契约 v1
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/conflicts")
@RequiredArgsConstructor
@Tag(name = "观点冲突", description = "认知卡片冲突检测、查询与管理相关接口")
public class CardConflictController {

    private final ConflictDetectionService conflictDetectionService;
    private final CardRepository cardRepository;

    @Operation(summary = "检测卡片冲突", description = "对指定卡片执行冲突检测，发现与其他卡片的观点冲突")
    @PostMapping("/detect/{cardId}")
    public ApiResponse<List<ConflictResponse>> detectConflicts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "卡片ID", required = true) @PathVariable Long cardId) {
        
        log.info("用户 {} 请求检测卡片 {} 的冲突", userDetails.getId(), cardId);
        
        List<CardConflict> conflicts = conflictDetectionService.detectConflicts(cardId);
        List<ConflictResponse> responses = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success(
            String.format("检测到 %d 个观点冲突", conflicts.size()),
            responses
        );
    }

    @Operation(summary = "获取未确认冲突", description = "获取用户所有未确认的观点冲突列表")
    @GetMapping("/unresolved")
    public ApiResponse<List<ConflictResponse>> getUnresolvedConflicts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        List<CardConflict> conflicts = conflictDetectionService.getUnresolvedConflicts(userDetails.getId());
        List<ConflictResponse> responses = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success("获取成功", responses);
    }

    @Operation(summary = "获取卡片相关冲突", description = "获取与指定卡片相关的所有冲突")
    @GetMapping("/card/{cardId}")
    public ApiResponse<List<ConflictResponse>> getConflictsByCard(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "卡片ID", required = true) @PathVariable Long cardId) {
        
        List<CardConflict> conflicts = conflictDetectionService.getConflictsByCard(cardId, userDetails.getId());
        List<ConflictResponse> responses = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success("获取成功", responses);
    }

    @Operation(summary = "获取冲突详情", description = "获取指定冲突的详细信息")
    @GetMapping("/{conflictId}")
    public ApiResponse<ConflictResponse> getConflictDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "冲突ID", required = true) @PathVariable Long conflictId) {
        
        // 查询冲突详情
        CardConflict conflict = conflictDetectionService.getConflictsByCard(conflictId, userDetails.getId())
                .stream()
                .findFirst()
                .orElse(null);
        
        if (conflict == null) {
            return ApiResponse.error(404, "冲突记录不存在");
        }
        
        return ApiResponse.success("获取成功", convertToResponse(conflict));
    }

    @Operation(summary = "确认冲突", description = "将冲突标记为已查看/已确认")
    @PostMapping("/{conflictId}/acknowledge")
    public ApiResponse<Void> acknowledgeConflict(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "冲突ID", required = true) @PathVariable Long conflictId) {
        
        conflictDetectionService.acknowledgeConflict(conflictId, userDetails.getId());
        return ApiResponse.success("冲突已确认", null);
    }

    @Operation(summary = "检查卡片间冲突", description = "检查两张卡片之间是否存在冲突")
    @GetMapping("/check")
    public ApiResponse<Boolean> checkConflictBetween(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "卡片1 ID", required = true) @RequestParam Long cardId1,
            @Parameter(description = "卡片2 ID", required = true) @RequestParam Long cardId2) {
        
        boolean hasConflict = conflictDetectionService.hasConflictBetween(
            cardId1, cardId2, userDetails.getId()
        );
        
        return ApiResponse.success("检查完成", hasConflict);
    }

    @Operation(summary = "获取未确认冲突数量", description = "获取用户未确认冲突的数量统计")
    @GetMapping("/count/unresolved")
    public ApiResponse<Long> getUnresolvedConflictCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        long count = conflictDetectionService.getUnresolvedConflictCount(userDetails.getId());
        return ApiResponse.success("获取成功", count);
    }

    /**
     * 转换为响应DTO
     */
    private ConflictResponse convertToResponse(CardConflict conflict) {
        // 获取卡片信息
        Card card1 = cardRepository.findById(conflict.getCardId1()).orElse(null);
        Card card2 = cardRepository.findById(conflict.getCardId2()).orElse(null);

        return ConflictResponse.builder()
                .id(conflict.getId())
                .cardId1(conflict.getCardId1())
                .cardId2(conflict.getCardId2())
                .cardTitle1(card1 != null ? card1.getTitle() : "未知卡片")
                .cardTitle2(card2 != null ? card2.getTitle() : "未知卡片")
                .cardViewpoint1(card1 != null ? card1.getOneSentenceSummary() : "")
                .cardViewpoint2(card2 != null ? card2.getOneSentenceSummary() : "")
                .conflictType(conflict.getConflictType())
                .conflictDescription(conflict.getConflictDescription())
                .topic(conflict.getTopic())
                .similarityScore(conflict.getSimilarityScore())
                .conflictScore(conflict.getConflictScore())
                .isAcknowledged(conflict.getIsAcknowledged())
                .aiAnalysis(conflict.getAiAnalysis())
                .createdAt(conflict.getCreatedAt())
                .build();
    }
}
