package com.evomind.controller;

import com.evomind.dto.request.ModerationRequest;
import com.evomind.dto.response.ApiResponse;
import com.evomind.dto.response.ModerationResponse;
import com.evomind.dto.response.SensitiveWordResponse;
import com.evomind.entity.ContentModerationLog;
import com.evomind.entity.SensitiveWord;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.ContentModerationService;
import com.evomind.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容审核控制器
 * 提供内容审核、敏感词管理等功能
 */
@RestController
@RequestMapping("/api/v1/moderation")
@RequiredArgsConstructor
@Tag(name = "内容审核", description = "内容审核、敏感词管理相关接口")
@SecurityRequirement(name = "bearer-jwt")
public class ContentModerationController {

    private final ContentModerationService contentModerationService;
    private final SensitiveWordService sensitiveWordService;

    /**
     * 内容审核（同步）
     */
    @PostMapping("/check")
    @Operation(summary = "内容审核", description = "提交内容进行审核")
    public ResponseEntity<ApiResponse<ModerationResponse>> moderateContent(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ModerationRequest request) {
        
        ModerationResponse response = contentModerationService.moderateContent(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 快速检测敏感词
     */
    @PostMapping("/quick-check")
    @Operation(summary = "快速敏感词检测", description = "快速检测文本是否包含敏感词")
    public ResponseEntity<ApiResponse<Map<String, Object>>> quickCheck(
            @RequestBody Map<String, String> request) {
        
        String content = request.get("content");
        boolean hasSensitiveWord = content != null && sensitiveWordService.containsSensitiveWord(content);
        List<ModerationResponse.HitWordInfo> hits = hasSensitiveWord ? 
                contentModerationService.quickCheckWithDetails(content) : null;
        
        Map<String, Object> result = new HashMap<>();
        result.put("hasSensitiveWord", hasSensitiveWord);
        result.put("hits", hits);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * AI生成内容审核
     */
    @PostMapping("/ai-content")
    @Operation(summary = "AI生成内容审核", description = "审核AI生成的内容")
    public ResponseEntity<ApiResponse<ModerationResponse>> moderateAiContent(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, String> request) {
        
        String content = request.get("content");
        String contentType = request.get("contentType");
        String aiModel = request.get("aiModel");
        
        ContentModerationLog.ContentType type = ContentModerationLog.ContentType.valueOf(contentType);
        ModerationResponse response = contentModerationService.moderateAiGeneratedContent(
                userDetails.getId(), content, type, aiModel);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取审核历史
     */
    @GetMapping("/history")
    @Operation(summary = "审核历史", description = "获取用户的审核历史记录")
    public ResponseEntity<ApiResponse<Page<ContentModerationLog>>> getModerationHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ContentModerationLog> history = contentModerationService.getUserModerationHistory(
                userDetails.getId(), pageable);
        
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * 获取审核统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "审核统计", description = "获取用户的审核统计数据")
    public ResponseEntity<ApiResponse<ContentModerationService.ModerationStatistics>> getStatistics(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        ContentModerationService.ModerationStatistics stats = 
                contentModerationService.getStatistics(userDetails.getId());
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ==================== 敏感词管理（管理员接口） ====================

    /**
     * 获取敏感词列表
     */
    @GetMapping("/sensitive-words")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "敏感词列表", description = "获取敏感词列表（管理员）")
    public ResponseEntity<ApiResponse<Page<SensitiveWordResponse>>> getSensitiveWords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SensitiveWord.WordCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("hitCount").descending());
        Page<SensitiveWord> words = sensitiveWordService.searchWords(keyword, category, pageable);
        Page<SensitiveWordResponse> response = words.map(SensitiveWordResponse::fromEntity);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 添加敏感词
     */
    @PostMapping("/sensitive-words")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "添加敏感词", description = "添加新的敏感词（管理员）")
    public ResponseEntity<ApiResponse<SensitiveWordResponse>> addSensitiveWord(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Object> request) {
        
        String word = (String) request.get("word");
        SensitiveWord.WordCategory category = SensitiveWord.WordCategory.valueOf((String) request.get("category"));
        SensitiveWord.SensitiveLevel level = SensitiveWord.SensitiveLevel.valueOf((String) request.get("level"));
        SensitiveWord.MatchMode matchMode = request.containsKey("matchMode") ? 
                SensitiveWord.MatchMode.valueOf((String) request.get("matchMode")) : SensitiveWord.MatchMode.CONTAINS;
        String description = (String) request.get("description");
        
        SensitiveWord saved = sensitiveWordService.addWord(word, category, level, matchMode, 
                                                             description, userDetails.getId());
        
        return ResponseEntity.ok(ApiResponse.success(SensitiveWordResponse.fromEntity(saved)));
    }

    /**
     * 更新敏感词
     */
    @PutMapping("/sensitive-words/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新敏感词", description = "更新敏感词信息（管理员）")
    public ResponseEntity<ApiResponse<SensitiveWordResponse>> updateSensitiveWord(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        Boolean enabled = request.containsKey("enabled") ? (Boolean) request.get("enabled") : null;
        SensitiveWord.SensitiveLevel level = request.containsKey("level") ? 
                SensitiveWord.SensitiveLevel.valueOf((String) request.get("level")) : null;
        String description = (String) request.get("description");
        
        SensitiveWord updated = sensitiveWordService.updateWord(id, enabled, level, description);
        
        return ResponseEntity.ok(ApiResponse.success(SensitiveWordResponse.fromEntity(updated)));
    }

    /**
     * 删除敏感词
     */
    @DeleteMapping("/sensitive-words/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除敏感词", description = "删除敏感词（管理员）")
    public ResponseEntity<ApiResponse<Void>> deleteSensitiveWord(@PathVariable Long id) {
        sensitiveWordService.deleteWord(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 批量导入敏感词
     */
    @PostMapping("/sensitive-words/batch-import")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量导入敏感词", description = "批量导入敏感词（管理员）")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchImportSensitiveWords(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        List<String> words = (List<String>) request.get("words");
        SensitiveWord.WordCategory category = SensitiveWord.WordCategory.valueOf((String) request.get("category"));
        SensitiveWord.SensitiveLevel level = SensitiveWord.SensitiveLevel.valueOf((String) request.get("level"));
        
        Map<String, Object> result = sensitiveWordService.batchImport(words, category, level, userDetails.getId());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 敏感词统计
     */
    @GetMapping("/sensitive-words/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "敏感词统计", description = "获取敏感词统计信息（管理员）")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSensitiveWordStatistics() {
        Map<String, Object> stats = sensitiveWordService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 热点敏感词
     */
    @GetMapping("/sensitive-words/hot")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "热点敏感词", description = "获取命中次数最多的敏感词（管理员）")
    public ResponseEntity<ApiResponse<List<SensitiveWordResponse>>> getHotSensitiveWords(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<SensitiveWord> hotWords = sensitiveWordService.getHotWords(limit);
        List<SensitiveWordResponse> response = hotWords.stream()
                .map(SensitiveWordResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 启用/禁用敏感词
     */
    @PutMapping("/sensitive-words/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "启用/禁用敏感词", description = "切换敏感词启用状态（管理员）")
    public ResponseEntity<ApiResponse<SensitiveWordResponse>> toggleSensitiveWord(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        
        boolean enabled = request.getOrDefault("enabled", true);
        SensitiveWord updated = sensitiveWordService.toggleEnabled(id, enabled);
        
        return ResponseEntity.ok(ApiResponse.success(SensitiveWordResponse.fromEntity(updated)));
    }

    // ==================== 人工复核（管理员接口） ====================

    /**
     * 人工复核
     */
    @PostMapping("/{logId}/manual-review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "人工复核", description = "对审核结果进行人工复核（管理员）")
    public ResponseEntity<ApiResponse<ModerationResponse>> manualReview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long logId,
            @RequestBody Map<String, String> request) {
        
        ContentModerationLog.ModerationStatus result = 
                ContentModerationLog.ModerationStatus.valueOf(request.get("result"));
        String remark = request.get("remark");
        
        ModerationResponse response = contentModerationService.manualReview(
                logId, userDetails.getId(), result, remark);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 重新审核
     */
    @PostMapping("/{logId}/re-moderate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重新审核", description = "重新对内容进行审核（管理员）")
    public ResponseEntity<ApiResponse<ModerationResponse>> reModerate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long logId) {
        
        ModerationResponse response = contentModerationService.reModerate(logId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}