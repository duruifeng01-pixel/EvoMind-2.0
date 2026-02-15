package com.evomind.controller;

import com.evomind.dto.request.CreateCorpusRequest;
import com.evomind.dto.response.ApiResponse;
import com.evomind.dto.response.UserCorpusDetailResponse;
import com.evomind.dto.response.UserCorpusResponse;
import com.evomind.entity.UserCorpus;
import com.evomind.entity.UserDetailsImpl;
import com.evomind.service.UserCorpusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户语料库控制器
 * 
 * 用户语料库存储用户生成的知识资产：
 * - 苏格拉底式对话洞察
 * - 用户笔记/随想
 * - 收藏高亮/标注
 * - AI辅助总结
 * 
 * 与 Card（认知卡片）的区别：
 * - Card：来自外部信息源，参与 Feed 流推荐
 * - UserCorpus：用户自己生成的内容，是用户的知识资产，不参与 Feed 流推荐
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/corpus")
@RequiredArgsConstructor
@Tag(name = "用户语料库", description = "用户知识资产管理接口")
public class UserCorpusController {

    private final UserCorpusService userCorpusService;

    @Operation(summary = "创建语料", description = "创建新的语料记录")
    @PostMapping
    public ApiResponse<UserCorpusResponse> createCorpus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CreateCorpusRequest request) {
        
        Long userId = userDetails.getId();
        UserCorpus corpus = userCorpusService.createCorpus(
            userId,
            request.getTitle(),
            request.getContentText(),
            request.getCorpusTypeEnum()
        );
        
        if (request.getSummaryText() != null) {
            corpus.setSummaryText(request.getSummaryText());
        }
        if (request.getOneSentenceSummary() != null) {
            corpus.setOneSentenceSummary(request.getOneSentenceSummary());
        }
        if (request.getSourceTypeEnum() != null) {
            corpus.setSourceType(request.getSourceTypeEnum());
        }
        if (request.getSourceId() != null) {
            corpus.setSourceId(request.getSourceId());
        }
        if (request.getSourceRef() != null) {
            corpus.setSourceRef(request.getSourceRef());
        }
        if (request.getDiscussionId() != null) {
            corpus.setDiscussionId(request.getDiscussionId());
        }
        if (request.getKeywords() != null) {
            corpus.setKeywords(request.getKeywords());
        }
        if (request.getReadingTimeMinutes() != null) {
            corpus.setReadingTimeMinutes(request.getReadingTimeMinutes());
        }
        if (request.getRelatedCardId() != null) {
            corpus.setRelatedCardId(request.getRelatedCardId());
        }
        
        return ApiResponse.success(UserCorpusResponse.fromEntity(corpus));
    }

    @Operation(summary = "获取用户语料库列表", description = "获取当前登录用户的语料库列表（默认排除归档内容）")
    @GetMapping
    public ApiResponse<Page<UserCorpusResponse>> getUserCorpus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size) {
        
        Long userId = userDetails.getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("isPinned").descending().and(Sort.by("createdAt").descending()));
        Page<UserCorpus> corpusPage = userCorpusService.getUserCorpus(userId, pageable);
        
        return ApiResponse.success(corpusPage.map(UserCorpusResponse::fromEntity));
    }

    @Operation(summary = "获取语料详情", description = "根据ID获取语料详情（自动记录查看次数）")
    @GetMapping("/{id}")
    public ApiResponse<UserCorpusDetailResponse> getCorpusDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语料ID") @PathVariable Long id) {
        
        Long userId = userDetails.getId();
        UserCorpus corpus = userCorpusService.getById(id)
                .orElseThrow(() -> new RuntimeException("语料不存在"));
        
        if (!corpus.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此语料");
        }
        
        // 记录查看
        userCorpusService.recordView(id);
        
        return ApiResponse.success(UserCorpusDetailResponse.fromEntity(corpus));
    }

    @Operation(summary = "获取归档的语料", description = "获取用户已归档的语料列表")
    @GetMapping("/archived")
    public ApiResponse<Page<UserCorpusResponse>> getArchivedCorpus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size) {
        
        Long userId = userDetails.getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("archivedAt").descending());
        Page<UserCorpus> corpusPage = userCorpusService.getArchivedCorpus(userId, pageable);
        
        return ApiResponse.success(corpusPage.map(UserCorpusResponse::fromEntity));
    }

    @Operation(summary = "获取收藏的语料", description = "获取用户收藏的语料列表")
    @GetMapping("/favorites")
    public ApiResponse<Page<UserCorpusResponse>> getFavoriteCorpus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size) {
        
        Long userId = userDetails.getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserCorpus> corpusPage = userCorpusService.getFavoriteCorpus(userId, pageable);
        
        return ApiResponse.success(corpusPage.map(UserCorpusResponse::fromEntity));
    }

    @Operation(summary = "按类型获取语料", description = "按类型筛选语料列表")
    @GetMapping("/type/{type}")
    public ApiResponse<Page<UserCorpusResponse>> getCorpusByType(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语料类型", example = "SOCRATIC_INSIGHT") @PathVariable String type,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size) {
        
        Long userId = userDetails.getId();
        UserCorpus.CorpusType corpusType = UserCorpus.CorpusType.valueOf(type);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserCorpus> corpusPage = userCorpusService.getCorpusByType(userId, corpusType, pageable);
        
        return ApiResponse.success(corpusPage.map(UserCorpusResponse::fromEntity));
    }

    @Operation(summary = "获取置顶的语料", description = "获取用户置顶的语料列表")
    @GetMapping("/pinned")
    public ApiResponse<List<UserCorpusResponse>> getPinnedCorpus(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        List<UserCorpus> corpusList = userCorpusService.getPinnedCorpus(userId);
        
        return ApiResponse.success(corpusList.stream()
                .map(UserCorpusResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "搜索语料", description = "根据关键词搜索语料标题和内容")
    @GetMapping("/search")
    public ApiResponse<Page<UserCorpusResponse>> searchCorpus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") int size) {
        
        Long userId = userDetails.getId();
        Pageable pageable = PageRequest.of(page, size);
        Page<UserCorpus> corpusPage = userCorpusService.searchCorpus(userId, keyword, pageable);
        
        return ApiResponse.success(corpusPage.map(UserCorpusResponse::fromEntity));
    }

    @Operation(summary = "收藏/取消收藏语料", description = "切换语料的收藏状态")
    @PostMapping("/{id}/favorite")
    public ApiResponse<UserCorpusResponse> toggleFavorite(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语料ID") @PathVariable Long id) {
        
        Long userId = userDetails.getId();
        UserCorpus corpus = userCorpusService.toggleFavorite(userId, id);
        
        return ApiResponse.success(UserCorpusResponse.fromEntity(corpus));
    }

    @Operation(summary = "置顶/取消置顶语料", description = "切换语料的置顶状态")
    @PostMapping("/{id}/pin")
    public ApiResponse<UserCorpusResponse> togglePin(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语料ID") @PathVariable Long id) {
        
        Long userId = userDetails.getId();
        UserCorpus corpus = userCorpusService.togglePin(userId, id);
        
        return ApiResponse.success(UserCorpusResponse.fromEntity(corpus));
    }

    @Operation(summary = "归档语料", description = "将语料归档")
    @PostMapping("/{id}/archive")
    public ApiResponse<UserCorpusResponse> archiveCorpus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语料ID") @PathVariable Long id) {
        
        Long userId = userDetails.getId();
        UserCorpus corpus = userCorpusService.archiveCorpus(userId, id);
        
        return ApiResponse.success(UserCorpusResponse.fromEntity(corpus));
    }

    @Operation(summary = "取消归档语料", description = "将语料从归档中恢复")
    @PostMapping("/{id}/unarchive")
    public ApiResponse<UserCorpusResponse> unarchiveCorpus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语料ID") @PathVariable Long id) {
        
        Long userId = userDetails.getId();
        UserCorpus corpus = userCorpusService.unarchiveCorpus(userId, id);
        
        return ApiResponse.success(UserCorpusResponse.fromEntity(corpus));
    }

    @Operation(summary = "删除语料", description = "永久删除语料")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCorpus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语料ID") @PathVariable Long id) {
        
        Long userId = userDetails.getId();
        userCorpusService.deleteCorpus(userId, id);
        
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取语料统计", description = "获取用户语料库的统计数据")
    @GetMapping("/stats")
    public ApiResponse<CorpusStatsResponse> getCorpusStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        CorpusStatsResponse stats = new CorpusStatsResponse();
        stats.setTotal(userCorpusService.countUserCorpus(userId));
        stats.setSocraticInsightCount(userCorpusService.countUserCorpusByType(userId, UserCorpus.CorpusType.SOCRATIC_INSIGHT));
        stats.setUserNoteCount(userCorpusService.countUserCorpusByType(userId, UserCorpus.CorpusType.USER_NOTE));
        stats.setHighlightCount(userCorpusService.countUserCorpusByType(userId, UserCorpus.CorpusType.HIGHLIGHT));
        stats.setAiSummaryCount(userCorpusService.countUserCorpusByType(userId, UserCorpus.CorpusType.AI_SUMMARY));
        
        return ApiResponse.success(stats);
    }

    @Operation(summary = "获取讨论相关的洞察", description = "获取与特定讨论相关的苏格拉底式对话洞察")
    @GetMapping("/discussion/{discussionId}")
    public ApiResponse<List<UserCorpusResponse>> getInsightsByDiscussion(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "讨论ID") @PathVariable Long discussionId) {
        
        Long userId = userDetails.getId();
        List<UserCorpus> corpusList = userCorpusService.getInsightsByDiscussion(userId, discussionId);
        
        return ApiResponse.success(corpusList.stream()
                .map(UserCorpusResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    /**
     * 语料统计响应
     */
    @lombok.Data
    public static class CorpusStatsResponse {
        private long total;
        private long socraticInsightCount;
        private long userNoteCount;
        private long highlightCount;
        private long aiSummaryCount;
    }
}
