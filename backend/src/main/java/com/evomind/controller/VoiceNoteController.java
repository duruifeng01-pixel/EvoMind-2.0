package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import com.evomind.dto.request.VoiceNoteRequest;
import com.evomind.dto.response.VoiceNoteResponse;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.VoiceNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 语音笔记控制器
 * 提供语音快速记录、转写、管理等功能
 */
@RestController
@RequestMapping("/api/v1/voice-notes")
@RequiredArgsConstructor
@Tag(name = "语音笔记", description = "语音快速记录、转写、管理相关接口")
@SecurityRequirement(name = "bearerAuth")
public class VoiceNoteController {

    private final VoiceNoteService voiceNoteService;

    /**
     * 上传音频文件创建语音笔记
     */
    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "上传音频创建语音笔记",
        description = "上传音频文件(MP3/WAV/PCM等)，自动进行语音转文字")
    public ResponseEntity<ApiResponse<VoiceNoteResponse>> createVoiceNote(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "音频文件") @RequestParam("audio") MultipartFile audioFile,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "标签，多个标签用逗号分隔") @RequestParam(required = false) String tags) {

        VoiceNoteRequest request = new VoiceNoteRequest();
        request.setTitle(title);
        request.setTags(tags);

        VoiceNoteResponse response = voiceNoteService.createVoiceNote(userDetails.getId(), audioFile, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 从Base64音频数据创建语音笔记
     */
    @PostMapping("/base64")
    @Operation(summary = "Base64音频创建语音笔记",
        description = "通过Base64编码的音频数据创建语音笔记，适用于客户端直接上传录音")
    public ResponseEntity<ApiResponse<VoiceNoteResponse>> createFromBase64(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody Map<String, Object> requestBody) {

        String audioBase64 = (String) requestBody.get("audioBase64");
        String format = (String) requestBody.getOrDefault("format", "pcm");
        Integer duration = (Integer) requestBody.get("duration");
        String title = (String) requestBody.get("title");
        String tags = (String) requestBody.get("tags");

        VoiceNoteRequest request = new VoiceNoteRequest();
        request.setTitle(title);
        request.setTags(tags);

        VoiceNoteResponse response = voiceNoteService.createFromBase64(
                userDetails.getId(), audioBase64, format, duration, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取语音笔记详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取语音笔记详情",
        description = "根据ID获取语音笔记的详细信息")
    public ResponseEntity<ApiResponse<VoiceNoteResponse>> getVoiceNote(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语音笔记ID") @PathVariable Long id) {

        VoiceNoteResponse response = voiceNoteService.getVoiceNote(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取用户的语音笔记列表
     */
    @GetMapping
    @Operation(summary = "获取语音笔记列表",
        description = "分页获取当前用户的所有语音笔记")
    public ResponseEntity<ApiResponse<Page<VoiceNoteResponse>>> getVoiceNotes(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<VoiceNoteResponse> page = voiceNoteService.getUserVoiceNotes(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 搜索语音笔记
     */
    @GetMapping("/search")
    @Operation(summary = "搜索语音笔记",
        description = "根据关键词搜索语音笔记的标题或转写内容")
    public ResponseEntity<ApiResponse<Page<VoiceNoteResponse>>> searchVoiceNotes(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<VoiceNoteResponse> page = voiceNoteService.searchVoiceNotes(userDetails.getId(), keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 更新语音笔记
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新语音笔记",
        description = "更新语音笔记的标题、标签、转写文本等信息")
    public ResponseEntity<ApiResponse<VoiceNoteResponse>> updateVoiceNote(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语音笔记ID") @PathVariable Long id,
            @Valid @RequestBody VoiceNoteRequest request) {

        VoiceNoteResponse response = voiceNoteService.updateVoiceNote(userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 删除语音笔记
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除语音笔记",
        description = "删除指定的语音笔记")
    public ResponseEntity<ApiResponse<Void>> deleteVoiceNote(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语音笔记ID") @PathVariable Long id) {

        voiceNoteService.deleteVoiceNote(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 收藏语音笔记
     */
    @PostMapping("/{id}/favorite")
    @Operation(summary = "收藏语音笔记",
        description = "将语音笔记标记为收藏")
    public ResponseEntity<ApiResponse<VoiceNoteResponse>> favoriteVoiceNote(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语音笔记ID") @PathVariable Long id) {

        VoiceNoteResponse response = voiceNoteService.toggleFavorite(userDetails.getId(), id, true);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 取消收藏语音笔记
     */
    @DeleteMapping("/{id}/favorite")
    @Operation(summary = "取消收藏语音笔记",
        description = "取消语音笔记的收藏标记")
    public ResponseEntity<ApiResponse<VoiceNoteResponse>> unfavoriteVoiceNote(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语音笔记ID") @PathVariable Long id) {

        VoiceNoteResponse response = voiceNoteService.toggleFavorite(userDetails.getId(), id, false);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 归档语音笔记
     */
    @PostMapping("/{id}/archive")
    @Operation(summary = "归档语音笔记",
        description = "将语音笔记归档（不在列表中显示）")
    public ResponseEntity<ApiResponse<VoiceNoteResponse>> archiveVoiceNote(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语音笔记ID") @PathVariable Long id) {

        VoiceNoteResponse response = voiceNoteService.toggleArchive(userDetails.getId(), id, true);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 取消归档语音笔记
     */
    @DeleteMapping("/{id}/archive")
    @Operation(summary = "取消归档语音笔记",
        description = "恢复已归档的语音笔记")
    public ResponseEntity<ApiResponse<VoiceNoteResponse>> unarchiveVoiceNote(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语音笔记ID") @PathVariable Long id) {

        VoiceNoteResponse response = voiceNoteService.toggleArchive(userDetails.getId(), id, false);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 重新转写语音笔记
     */
    @PostMapping("/{id}/retranscribe")
    @Operation(summary = "重新转写语音笔记",
        description = "对转写失败或质量不佳的语音笔记重新进行转写")
    public ResponseEntity<ApiResponse<VoiceNoteResponse>> retranscribe(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "语音笔记ID") @PathVariable Long id) {

        VoiceNoteResponse response = voiceNoteService.retranscribe(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取收藏的语音笔记
     */
    @GetMapping("/favorites")
    @Operation(summary = "获取收藏的语音笔记",
        description = "获取用户收藏的语音笔记列表")
    public ResponseEntity<ApiResponse<Page<VoiceNoteResponse>>> getFavoriteVoiceNotes(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<VoiceNoteResponse> page = voiceNoteService.getFavoriteVoiceNotes(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 获取语音笔记统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取语音笔记统计",
        description = "获取用户的语音笔记统计数据，包括总数、今日数量、总时长等")
    public ResponseEntity<ApiResponse<VoiceNoteService.VoiceNoteStatistics>> getStatistics(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        VoiceNoteService.VoiceNoteStatistics stats = voiceNoteService.getStatistics(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
