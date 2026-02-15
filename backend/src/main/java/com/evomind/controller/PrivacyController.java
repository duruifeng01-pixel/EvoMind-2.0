package com.evomind.controller;

import com.evomind.dto.response.ApiResponse;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.PrivacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 隐私与数据权利控制器
 * 处理数据导出、账号注销等用户隐私相关功能
 */
@RestController
@RequestMapping("/api/v1/privacy")
@RequiredArgsConstructor
@Tag(name = "隐私与数据权利", description = "数据导出、账号注销等隐私功能")
public class PrivacyController {

    private final PrivacyService privacyService;

    /**
     * 导出用户所有数据
     */
    @PostMapping("/export")
    @Operation(summary = "导出用户数据", description = "导出用户的所有个人数据，包括用户信息、卡片、语料库、对话记录等")
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportUserData(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, Object> userData = privacyService.exportUserData(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(userData));
    }

    /**
     * 申请注销账号
     */
    @PostMapping("/delete-account")
    @Operation(summary = "申请注销账号", description = "提交账号注销申请，进入冷静期后可正式注销")
    public ResponseEntity<ApiResponse<Map<String, String>>> requestAccountDeletion(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String deletionToken = privacyService.requestAccountDeletion(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "message", "账号注销申请已提交，请在7天内确认",
            "deletionToken", deletionToken,
            "cooldownDays", "7"
        )));
    }

    /**
     * 确认注销账号
     */
    @PostMapping("/confirm-deletion")
    @Operation(summary = "确认注销账号", description = "使用deletionToken确认正式注销账号")
    public ResponseEntity<ApiResponse<Void>> confirmAccountDeletion(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String deletionToken) {
        privacyService.confirmAccountDeletion(userDetails.getId(), deletionToken);
        return ResponseEntity.ok(ApiResponse.success(null, "账号已成功注销"));
    }

    /**
     * 取消注销申请
     */
    @PostMapping("/cancel-deletion")
    @Operation(summary = "取消注销申请", description = "在冷静期内取消账号注销申请")
    public ResponseEntity<ApiResponse<Void>> cancelAccountDeletion(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        privacyService.cancelAccountDeletion(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "账号注销申请已取消"));
    }

    /**
     * 获取数据导出历史
     */
    @GetMapping("/export-history")
    @Operation(summary = "获取导出历史", description = "获取用户数据导出的历史记录")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExportHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<String, Object> history = privacyService.getExportHistory(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
