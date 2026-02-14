package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import com.evomind.dto.request.CreateCardRequest;
import com.evomind.dto.response.CardResponse;
import com.evomind.dto.response.DrilldownResponse;
import com.evomind.dto.response.MindMapResponse;
import com.evomind.entity.Card;
import com.evomind.entity.MindMapNode;
import com.evomind.entity.SourceContent;
import com.evomind.entity.User;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "认知卡片", description = "认知卡片相关接口")
@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final MindMapService mindMapService;
    private final SourceContentService sourceContentService;
    private final ConflictDetectionService conflictDetectionService;

    @Operation(summary = "获取卡片Feed流", description = "7:3混合流获取认知卡片")
    @GetMapping("/feed")
    public ApiResponse<List<CardResponse>> getFeed(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = userDetails.getId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards = cardService.getCardsByUserId(userId, pageable);
        
        List<CardResponse> responses = cards.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success("获取成功", responses);
    }

    @Operation(summary = "获取卡片详情")
    @GetMapping("/{id}")
    public ApiResponse<CardResponse> getCard(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        Card card = cardService.getCardById(id, userDetails.getId());
        cardService.incrementViewCount(id);
        return ApiResponse.success("获取成功", convertToResponse(card));
    }

    @Operation(summary = "创建卡片")
    @PostMapping
    public ApiResponse<CardResponse> createCard(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateCardRequest request) {
        
        Card card = cardService.createCard(
                userDetails.getId(),
                request.getTitle(),
                request.getSummaryText(),
                request.getSourceId(),
                request.getSourceUrl(),
                request.getMindmapJson()
        );
        
        // 保存脑图节点
        if (request.getMindmapJson() != null && !request.getMindmapJson().isEmpty()) {
            mindMapService.saveMindMapNodes(card.getId(), request.getMindmapJson());
        }
        
        // 更新额外字段
        card.setOneSentenceSummary(request.getOneSentenceSummary());
        card.setSourceTitle(request.getSourceTitle());
        card.setKeywords(request.getKeywords());
        card.setReadingTimeMinutes(request.getReadingTimeMinutes());
        
        return ApiResponse.success("创建成功", convertToResponse(card));
    }

    @Operation(summary = "获取脑图")
    @GetMapping("/{id}/mindmap")
    public ApiResponse<MindMapResponse> getMindMap(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        Card card = cardService.getCardById(id, userDetails.getId());
        List<MindMapNode> nodes = mindMapService.getMindMapNodes(id);
        
        MindMapResponse response = MindMapResponse.builder()
                .cardId(id)
                .cardTitle(card.getTitle())
                .nodes(buildNodeTree(nodes))
                .totalNodes(nodes.size())
                .build();
        
        return ApiResponse.success("获取成功", response);
    }

    @Operation(summary = "脑图下钻 - 获取原文段落")
    @GetMapping("/{id}/drilldown")
    public ApiResponse<DrilldownResponse> drillDown(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id,
            @RequestParam String nodeId) {
        
        Card card = cardService.getCardById(id, userDetails.getId());
        List<MindMapNode> nodes = mindMapService.getMindMapNodes(id);
        
        MindMapNode targetNode = nodes.stream()
                .filter(n -> n.getNodeId().equals(nodeId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("节点不存在"));
        
        String paragraph = null;
        SourceContent sourceContent = null;
        
        if (Boolean.TRUE.equals(targetNode.getHasOriginalReference()) 
                && targetNode.getOriginalContentId() != null) {
            sourceContent = sourceContentService.getContentBySourceId(targetNode.getOriginalContentId())
                    .orElse(null);
            if (sourceContent != null && targetNode.getOriginalParagraphIndex() != null) {
                paragraph = sourceContentService.getParagraph(
                        sourceContent.getId(), 
                        targetNode.getOriginalParagraphIndex()
                );
            }
        }
        
        DrilldownResponse response = DrilldownResponse.builder()
                .cardId(id)
                .nodeId(nodeId)
                .nodeText(targetNode.getText())
                .originalParagraph(paragraph)
                .paragraphIndex(targetNode.getOriginalParagraphIndex())
                .sourceContentId(targetNode.getOriginalContentId())
                .sourceTitle(sourceContent != null ? sourceContent.getTitle() : null)
                .sourceAuthor(sourceContent != null ? sourceContent.getAuthor() : null)
                .warningMessage("AI生成，仅供参考")
                .build();
        
        return ApiResponse.success("获取成功", response);
    }

    @Operation(summary = "收藏/取消收藏卡片")
    @PostMapping("/{id}/favorite")
    public ApiResponse<Void> toggleFavorite(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        cardService.toggleFavorite(id, userDetails.getId());
        return ApiResponse.success("操作成功", null);
    }

    @Operation(summary = "归档卡片")
    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archiveCard(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        cardService.archiveCard(id, userDetails.getId());
        return ApiResponse.success("归档成功", null);
    }

    @Operation(summary = "搜索卡片")
    @GetMapping("/search")
    public ApiResponse<List<CardResponse>> searchCards(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String keyword) {
        
        List<Card> cards = cardService.searchCards(userDetails.getId(), keyword);
        List<CardResponse> responses = cards.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ApiResponse.success("搜索成功", responses);
    }

    @Operation(summary = "获取冲突标记")
    @GetMapping("/{id}/conflicts")
    public ApiResponse<List<Map<String, Object>>> getConflicts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long id) {
        
        var conflicts = conflictDetectionService.getConflictsByCard(id, userDetails.getId());
        List<Map<String, Object>> responses = conflicts.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("cardId1", c.getCardId1());
            map.put("cardId2", c.getCardId2());
            map.put("conflictType", c.getConflictType());
            map.put("conflictDescription", c.getConflictDescription());
            map.put("topic", c.getTopic());
            map.put("conflictScore", c.getConflictScore());
            map.put("isAcknowledged", c.getIsAcknowledged());
            return map;
        }).collect(Collectors.toList());
        
        return ApiResponse.success("获取成功", responses);
    }

    private CardResponse convertToResponse(Card card) {
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
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }

    private List<MindMapResponse.MindMapNodeResponse> buildNodeTree(List<MindMapNode> nodes) {
        Map<String, MindMapResponse.MindMapNodeResponse> nodeMap = new HashMap<>();
        List<MindMapResponse.MindMapNodeResponse> roots = new ArrayList<>();
        
        // 创建所有节点
        for (MindMapNode node : nodes) {
            MindMapResponse.MindMapNodeResponse response = MindMapResponse.MindMapNodeResponse.builder()
                    .nodeId(node.getNodeId())
                    .parentNodeId(node.getParentNodeId())
                    .text(node.getText())
                    .description(node.getDescription())
                    .nodeType(node.getNodeType())
                    .level(node.getLevel())
                    .sortOrder(node.getSortOrder())
                    .hasOriginalReference(node.getHasOriginalReference())
                    .originalContentId(node.getOriginalContentId())
                    .originalParagraphIndex(node.getOriginalParagraphIndex())
                    .isExpanded(node.getIsExpanded())
                    .children(new ArrayList<>())
                    .build();
            nodeMap.put(node.getNodeId(), response);
        }
        
        // 构建树结构
        for (MindMapResponse.MindMapNodeResponse node : nodeMap.values()) {
            if (node.getParentNodeId() == null || node.getParentNodeId().isEmpty()) {
                roots.add(node);
            } else {
                MindMapResponse.MindMapNodeResponse parent = nodeMap.get(node.getParentNodeId());
                if (parent != null && parent.getChildren() != null) {
                    parent.getChildren().add(node);
                }
            }
        }
        
        // 排序
        roots.sort(Comparator.comparing(MindMapResponse.MindMapNodeResponse::getSortOrder));
        roots.forEach(this::sortChildren);
        
        return roots;
    }

    private void sortChildren(MindMapResponse.MindMapNodeResponse node) {
        if (node.getChildren() != null) {
            node.getChildren().sort(Comparator.comparing(MindMapResponse.MindMapNodeResponse::getSortOrder));
            node.getChildren().forEach(this::sortChildren);
        }
    }
}
