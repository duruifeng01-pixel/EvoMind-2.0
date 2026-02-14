package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import com.evomind.dto.request.AiGenerateCardRequest;
import com.evomind.dto.response.AiGeneratedContentResponse;
import com.evomind.dto.response.CardResponse;
import com.evomind.entity.Card;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.AiGenerationService;
import com.evomind.service.CardService;
import com.evomind.service.MindMapService;
import com.evomind.service.SourceContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * AI认知卡片生成控制器
 */
@Slf4j
@Tag(name = "AI认知卡片", description = "AI生成认知卡片相关接口")
@RestController
@RequestMapping("/api/v1/ai-cards")
@RequiredArgsConstructor
public class AiCardController {

    private final AiGenerationService aiGenerationService;
    private final CardService cardService;
    private final MindMapService mindMapService;
    private final SourceContentService sourceContentService;

    /**
     * AI生成认知卡片内容（预览）
     */
    @Operation(summary = "AI生成卡片内容预览", description = "根据原文生成认知卡片的核心内容，不保存")
    @PostMapping("/preview")
    public ApiResponse<AiGeneratedContentResponse> previewCardContent(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody AiGenerateCardRequest request) {
        
        log.info("用户 {} 请求AI生成卡片预览", userDetails.getId());
        
        AiGeneratedContentResponse response = aiGenerationService.generateCardContent(
                request.getOriginalContent(),
                request.getTitle(),
                request.getGenerateMindMap()
        );
        
        return ApiResponse.success("生成成功", response);
    }

    /**
     * AI生成并保存认知卡片
     */
    @Operation(summary = "AI生成并保存卡片", description = "根据原文生成认知卡片并保存到用户的语料库")
    @PostMapping("/generate")
    public ApiResponse<CardResponse> generateAndSaveCard(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody AiGenerateCardRequest request) {
        
        Long userId = userDetails.getId();
        log.info("用户 {} 请求AI生成并保存卡片", userId);
        
        // 1. 调用AI生成内容
        AiGeneratedContentResponse aiContent = aiGenerationService.generateCardContent(
                request.getOriginalContent(),
                request.getTitle(),
                request.getGenerateMindMap()
        );
        
        // 2. 保存原文内容（如果来源是外部链接或粘贴内容）
        Long sourceContentId = null;
        if (request.getOriginalContent() != null && request.getOriginalContent().length() > 100) {
            // 保存原文到 SourceContent
            var sourceContent = sourceContentService.saveContent(
                    userId,
                    request.getSourceId(),
                    aiContent.getTitle(),
                    request.getOriginalContent(),
                    request.getSourceUrl(),
                    "ai_generated"
            );
            sourceContentId = sourceContent.getId();
        }
        
        // 3. 创建卡片
        String keywordsStr = String.join(",", aiContent.getKeywords());
        
        Card card = cardService.createCard(
                userId,
                aiContent.getTitle(),
                aiContent.getSummaryText(),
                request.getSourceId(),
                request.getSourceUrl(),
                aiContent.getMindMapJson()
        );
        
        // 4. 更新卡片AI生成相关字段
        card.setOneSentenceSummary(aiContent.getOneSentenceSummary());
        card.setKeywords(keywordsStr);
        card.setReadingTimeMinutes(aiContent.getReadingTimeMinutes());
        card.setOriginalContentId(sourceContentId);
        card.setGenerateStatus("COMPLETED");
        card.setAiModel("deepseek-chat");
        card.setTokenUsed(aiContent.getTokenUsed());
        
        // 5. 保存脑图节点
        if (aiContent.getMindMapJson() != null && !aiContent.getMindMapJson().isEmpty()) {
            mindMapService.saveMindMapNodes(card.getId(), aiContent.getMindMapJson());
        }
        
        log.info("AI卡片生成并保存成功: cardId={}, userId={}, tokens={}", 
                card.getId(), userId, aiContent.getTokenUsed());
        
        return ApiResponse.success("生成并保存成功", convertToResponse(card, aiContent));
    }

    /**
     * 根据来源内容生成卡片
     */
    @Operation(summary = "根据来源生成卡片", description = "根据已导入的信息源内容生成认知卡片")
    @PostMapping("/generate-from-source/{sourceId}")
    public ApiResponse<CardResponse> generateFromSource(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long sourceId,
            @RequestParam(required = false) String content,
            @RequestParam(defaultValue = "true") boolean generateMindMap) {
        
        Long userId = userDetails.getId();
        log.info("用户 {} 根据来源 {} 生成卡片", userId, sourceId);
        
        // 获取来源内容
        String originalContent = content;
        if (originalContent == null || originalContent.isEmpty()) {
            var sourceContent = sourceContentService.getContentBySourceId(sourceId);
            if (sourceContent.isPresent()) {
                originalContent = sourceContent.get().getContent();
            } else {
                return ApiResponse.error(404, "来源内容不存在");
            }
        }
        
        // 生成卡片
        AiGeneratedContentResponse aiContent = aiGenerationService.generateCardContent(
                originalContent,
                null,
                generateMindMap
        );
        
        // 创建并保存卡片
        Card card = cardService.createCard(
                userId,
                aiContent.getTitle(),
                aiContent.getSummaryText(),
                sourceId,
                null,
                aiContent.getMindMapJson()
        );
        
        card.setOneSentenceSummary(aiContent.getOneSentenceSummary());
        card.setKeywords(String.join(",", aiContent.getKeywords()));
        card.setReadingTimeMinutes(aiContent.getReadingTimeMinutes());
        card.setGenerateStatus("COMPLETED");
        card.setAiModel("deepseek-chat");
        card.setTokenUsed(aiContent.getTokenUsed());
        
        if (aiContent.getMindMapJson() != null) {
            mindMapService.saveMindMapNodes(card.getId(), aiContent.getMindMapJson());
        }
        
        return ApiResponse.success("生成成功", convertToResponse(card, aiContent));
    }

    /**
     * 转换响应
     */
    private CardResponse convertToResponse(Card card, AiGeneratedContentResponse aiContent) {
        return CardResponse.builder()
                .id(card.getId())
                .title(card.getTitle())
                .summaryText(card.getSummaryText())
                .oneSentenceSummary(card.getOneSentenceSummary())
                .sourceId(card.getSourceId())
                .sourceUrl(card.getSourceUrl())
                .sourceTitle(card.getSourceTitle())
                .isFavorite(card.getIsFavorite())
                .isArchived(card.getIsArchived())
                .hasConflict(card.getHasConflict())
                .viewCount(card.getViewCount())
                .lastViewedAt(card.getLastViewedAt())
                .generateStatus(card.getGenerateStatus())
                .keywords(card.getKeywords())
                .readingTimeMinutes(card.getReadingTimeMinutes())
                .tokenUsed(card.getTokenUsed())
                .aiModel(card.getAiModel())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                // AI生成的额外信息
                .goldenQuotes(aiContent != null ? aiContent.getGoldenQuotes() : null)
                .cases(aiContent != null ? aiContent.getCases() : null)
                .build();
    }
}
