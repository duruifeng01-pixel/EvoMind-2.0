package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import com.evomind.dto.request.ConfirmImportRequest;
import com.evomind.dto.request.OcrImportRequest;
import com.evomind.dto.response.OcrResultResponse;
import com.evomind.dto.response.SourceImportJobResponse;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.OcrService;
import com.evomind.service.SourceImportService;
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

import java.util.List;
import java.util.Map;

/**
 * 信息源管理控制器
 * 支持OCR截图导入、链接抓取导入等方式管理信息源
 */
@RestController
@RequestMapping("/api/v1/sources")
@RequiredArgsConstructor
@Tag(name = "信息源管理", description = "信息源导入、查询、管理相关接口")
@SecurityRequirement(name = "bearerAuth")
public class SourceController {

    private final SourceImportService sourceImportService;
    private final OcrService ocrService;

    /**
     * OCR截图识别导入信息源
     */
    @PostMapping("/ocr-import")
    @Operation(summary = "OCR截图识别导入",
        description = "上传截图（小红书/微信关注列表等），OCR识别博主名称并生成待导入列表")
    public ResponseEntity<ApiResponse<OcrResultResponse>> ocrImport(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody OcrImportRequest request) {

        OcrResultResponse result = ocrService.recognizeBloggers(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 确认导入OCR识别的博主
     */
    @PostMapping("/confirm-import")
    @Operation(summary = "确认导入OCR识别结果",
        description = "从OCR识别结果中选择要导入的博主，一键添加到个人信息源")
    public ResponseEntity<ApiResponse<List<SourceImportService.ImportResult>>> confirmImport(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ConfirmImportRequest request) {

        List<SourceImportService.ImportResult> results = sourceImportService.confirmImport(
            userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * 获取OCR识别结果
     */
    @GetMapping("/ocr-result/{taskId}")
    @Operation(summary = "获取OCR识别结果",
        description = "根据任务ID查询OCR识别结果")
    public ResponseEntity<ApiResponse<OcrResultResponse>> getOcrResult(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "OCR任务ID") @PathVariable String taskId) {

        OcrResultResponse result = ocrService.getResultByTaskId(taskId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 重新识别OCR任务
     */
    @PostMapping("/ocr-retry/{taskId}")
    @Operation(summary = "重新识别OCR任务",
        description = "对失败的OCR任务重新进行识别")
    public ResponseEntity<ApiResponse<OcrResultResponse>> retryOcr(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "OCR任务ID") @PathVariable String taskId,
            @RequestBody OcrImportRequest request) {

        // 获取原任务信息，重新识别
        OcrResultResponse result = ocrService.recognizeBloggers(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取用户的导入任务列表
     */
    @GetMapping("/import-jobs")
    @Operation(summary = "获取导入任务列表",
        description = "分页查询用户的信息源导入任务历史")
    public ResponseEntity<ApiResponse<Page<SourceImportJobResponse>>> getImportJobs(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<SourceImportJobResponse> jobs = sourceImportService.getUserJobs(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    /**
     * 获取导入任务详情
     */
    @GetMapping("/import-jobs/{jobId}")
    @Operation(summary = "获取导入任务详情",
        description = "查询指定导入任务的详细状态和识别结果")
    public ResponseEntity<ApiResponse<SourceImportJobResponse>> getJobDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID") @PathVariable Long jobId) {

        SourceImportJobResponse job = sourceImportService.getJobStatus(userDetails.getId(), jobId);
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    /**
     * 取消导入任务
     */
    @PostMapping("/import-jobs/{jobId}/cancel")
    @Operation(summary = "取消导入任务",
        description = "取消进行中的导入任务")
    public ResponseEntity<ApiResponse<Void>> cancelJob(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID") @PathVariable Long jobId) {

        sourceImportService.cancelJob(userDetails.getId(), jobId);
        return ResponseEntity.ok(ApiResponse.success(null, "任务已取消"));
    }

    /**
     * 重试失败的导入任务
     */
    @PostMapping("/import-jobs/{jobId}/retry")
    @Operation(summary = "重试导入任务",
        description = "对失败的导入任务进行重试")
    public ResponseEntity<ApiResponse<SourceImportJobResponse>> retryJob(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID") @PathVariable Long jobId) {

        SourceImportJobResponse job = sourceImportService.retryJob(userDetails.getId(), jobId);
        return ResponseEntity.ok(ApiResponse.success(job));
    }

    /**
     * 检查今日导入次数
     */
    @GetMapping("/import-limit")
    @Operation(summary = "查询今日导入限额",
        description = "获取今日已使用导入次数和剩余次数")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getImportLimit(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        int dailyLimit = 20;
        boolean exceeded = sourceImportService.isDailyLimitExceeded(userDetails.getId(), dailyLimit);

        Map<String, Object> limitInfo = Map.of(
            "dailyLimit", dailyLimit,
            "isExceeded", exceeded,
            "remaining", exceeded ? 0 : dailyLimit - getTodayImportCount(userDetails.getId())
        );

        return ResponseEntity.ok(ApiResponse.success(limitInfo));
    }

    /**
     * 获取今日导入次数（辅助方法）
     */
    private int getTodayImportCount(Long userId) {
        // 简化实现，实际应从服务层获取
        return 0;
    }
}
