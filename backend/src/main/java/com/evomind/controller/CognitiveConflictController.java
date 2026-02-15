package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import com.evomind.dto.response.CognitiveConflictResponse;
import com.evomind.entity.Card;
import com.evomind.entity.CognitiveConflict;
import com.evomind.entity.UserCognitiveProfile;
import com.evomind.repository.CardRepository;
import com.evomind.repository.UserCognitiveProfileRepository;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.CognitiveConflictService;
import com.evomind.service.CognitiveProfileService;
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
 * 认知冲突控制器
 * 新卡片与用户认知体系冲突检测API - 符合API契约 v1
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cognitive-conflicts")
@RequiredArgsConstructor
@Tag(name = "认知冲突", description = "新卡片与用户认知体系冲突检测与管理相关接口")
public class CognitiveConflictController {

    private final CognitiveConflictService conflictService;
    private final CognitiveProfileService profileService;
    private final CardRepository cardRepository;
    private final UserCognitiveProfileRepository profileRepository;

    @Operation(summary = "检测卡片冲突", description = "检测指定卡片与用户认知体系的冲突")
    @PostMapping("/detect/{cardId}")
    public ApiResponse<List<CognitiveConflictResponse>> detectConflicts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "卡片ID", required = true) @PathVariable Long cardId) {
        
        log.info("用户 {} 请求检测卡片 {} 的认知冲突", userDetails.getId(), cardId);
        
        Card card = cardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("卡片不存在"));
        
        List<CognitiveConflict> conflicts = conflictService.detectConflicts(userDetails.getId(), card);
        List<CognitiveConflictResponse> responses = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success(
            String.format("检测到 %d 个认知冲突", conflicts.size()),
            responses
        );
    }

    @Operation(summary = "获取未确认冲突", description = "获取用户所有未处理的认知冲突")
    @GetMapping("/unresolved")
    public ApiResponse<List<CognitiveConflictResponse>> getUnresolvedConflicts(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        List<CognitiveConflict> conflicts = conflictService.getUnresolvedConflicts(userDetails.getId());
        List<CognitiveConflictResponse> responses = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success("获取成功", responses);
    }

    @Operation(summary = "获取卡片冲突", description = "获取指定卡片的所有认知冲突")
    @GetMapping("/card/{cardId}")
    public ApiResponse<List<CognitiveConflictResponse>> getConflictsByCard(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "卡片ID", required = true) @PathVariable Long cardId) {
        
        List<CognitiveConflict> conflicts = conflictService.getConflictsByCard(cardId, userDetails.getId());
        List<CognitiveConflictResponse> responses = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success("获取成功", responses);
    }

    @Operation(summary = "确认冲突", description = "将冲突标记为已查看/已确认")
    @PostMapping("/{conflictId}/acknowledge")
    public ApiResponse<Void> acknowledgeConflict(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "冲突ID", required = true) @PathVariable Long conflictId) {
        
        conflictService.acknowledgeConflict(conflictId, userDetails.getId());
        return ApiResponse.success("冲突已确认", null);
    }

    @Operation(summary = "忽略冲突", description = "忽略此冲突提醒")
    @PostMapping("/{conflictId}/dismiss")
    public ApiResponse<Void> dismissConflict(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "冲突ID", required = true) @PathVariable Long conflictId) {
        
        conflictService.dismissConflict(conflictId, userDetails.getId());
        return ApiResponse.success("冲突已忽略", null);
    }

    @Operation(summary = "获取未确认冲突数量", description = "获取用户未处理冲突的数量统计")
    @GetMapping("/count/unresolved")
    public ApiResponse<Long> getUnresolvedConflictCount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        long count = conflictService.getUnresolvedConflictCount(userDetails.getId());
        return ApiResponse.success("获取成功", count);
    }

    @Operation(summary = "获取用户认知画像", description = "获取用户的认知体系画像")
    @GetMapping("/profiles")
    public ApiResponse<List<UserCognitiveProfile>> getUserProfiles(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        List<UserCognitiveProfile> profiles = profileService.getUserProfiles(userDetails.getId());
        return ApiResponse.success("获取成功", profiles);
    }

    @Operation(summary = "重建认知画像", description = "根据用户所有卡片重新构建认知画像")
    @PostMapping("/profiles/rebuild")
    public ApiResponse<Void> rebuildProfiles(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        profileService.buildCognitiveProfile(userDetails.getId());
        return ApiResponse.success("认知画像重建中", null);
    }

    /**
     * 转换为响应DTO
     */
    private CognitiveConflictResponse convertToResponse(CognitiveConflict conflict) {
        Card card = cardRepository.findById(conflict.getCardId()).orElse(null);
        
        return CognitiveConflictResponse.builder()
                .id(conflict.getId())
                .cardId(conflict.getCardId())
                .cardTitle(card != null ? card.getTitle() : "未知卡片")
                .cardViewpoint(conflict.getCardViewpoint())
                .profileId(conflict.getProfileId())
                .topic(conflict.getTopic())
                .userBelief(conflict.getUserBelief())
                .conflictType(conflict.getConflictType())
                .conflictDescription(conflict.getConflictDescription())
                .conflictScore(conflict.getConflictScore())
                .isAcknowledged(conflict.getIsAcknowledged())
                .isDismissed(conflict.getIsDismissed())
                .aiAnalysis(conflict.getAiAnalysis())
                .createdAt(conflict.getCreatedAt())
                .build();
    }
}
