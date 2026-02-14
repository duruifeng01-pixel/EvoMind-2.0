package com.evomind.controller;

import com.evomind.dto.request.ConfirmImportRequest;
import com.evomind.dto.request.LinkImportRequest;
import com.evomind.dto.request.OcrImportRequest;
import com.evomind.dto.response.ApiResponse;
import com.evomind.dto.response.SourceImportJobResponse;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.SourceImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 信息源导入控制器
 * 支持OCR截图识别和链接抓取导入信息源
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sources/import")
@RequiredArgsConstructor
@Tag(name = "信息源导入", description = "OCR截图识别和链接抓取导入信息源")
public class SourceImportController {

    private final SourceImportService sourceImportService;

    /**
     * OCR截图识别导入
     */
    @PostMapping("/ocr")
    @Operation(summary = "OCR截图识别", description = "上传截图，OCR识别其中的博主信息")
    public ResponseEntity<ApiResponse<SourceImportJobResponse>> ocrImport(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody OcrImportRequest request) {

        Long userId = userDetails.getId();
        log.info("用户 {} 提交OCR截图导入请求", userId);

        SourceImportJobResponse response = sourceImportService.submitOcrTask(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 链接抓取导入
     */
    @PostMapping("/link")
    @Operation(summary = "链接抓取导入", description = "提交文章/笔记链接，抓取作者信息")
    public ResponseEntity<ApiResponse<SourceImportJobResponse>> linkImport(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody LinkImportRequest request) {

        Long userId = userDetails.getId();
        log.info("用户 {} 提交链接抓取导入请求: {}", userId, request.getUrl());

        SourceImportJobResponse response = sourceImportService.submitLinkTask(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "获取任务状态", description = "查询指定导入任务的当前状态")
    public ResponseEntity<ApiResponse<SourceImportJobResponse>> getJobStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID") @PathVariable Long jobId) {

        Long userId = userDetails.getId();
        SourceImportJobResponse response = sourceImportService.getJobStatus(userId, jobId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取用户的导入任务列表
     */
    @GetMapping("/jobs")
    @Operation(summary = "获取任务列表", description = "分页查询用户的导入任务历史")
    public ResponseEntity<ApiResponse<Page<SourceImportJobResponse>>> getUserJobs(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {

        Long userId = userDetails.getId();
        Page<SourceImportJobResponse> response = sourceImportService.getUserJobs(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 确认导入选中的作者
     */
    @PostMapping("/confirm")
    @Operation(summary = "确认导入", description = "从检测到的作者列表中选择要添加的信息源")
    public ResponseEntity<ApiResponse<List<SourceImportService.ImportResult>>> confirmImport(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ConfirmImportRequest request) {

        Long userId = userDetails.getId();
        log.info("用户 {} 确认导入 {} 个信息源", userId, request.getSelectedAuthors().size());

        List<SourceImportService.ImportResult> results = sourceImportService.confirmImport(userId, request);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * 取消导入任务
     */
    @PostMapping("/jobs/{jobId}/cancel")
    @Operation(summary = "取消任务", description = "取消待处理或处理中的导入任务")
    public ResponseEntity<ApiResponse<Void>> cancelJob(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID") @PathVariable Long jobId) {

        Long userId = userDetails.getId();
        sourceImportService.cancelJob(userId, jobId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 重试失败的任务
     */
    @PostMapping("/jobs/{jobId}/retry")
    @Operation(summary = "重试任务", description = "重新执行失败的导入任务")
    public ResponseEntity<ApiResponse<SourceImportJobResponse>> retryJob(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "任务ID") @PathVariable Long jobId) {

        Long userId = userDetails.getId();
        SourceImportJobResponse response = sourceImportService.retryJob(userId, jobId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
