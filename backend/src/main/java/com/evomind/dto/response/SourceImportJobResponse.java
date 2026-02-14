package com.evomind.dto.response;

import com.evomind.entity.SourceImportJob;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 信息源导入任务响应DTO
 */
@Data
@Schema(description = "信息源导入任务响应")
public class SourceImportJobResponse {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "导入类型：OCR_SCREENSHOT/OCR截图识别、LINK_SCRAPE/链接抓取")
    private String importType;

    @Schema(description = "任务状态：PENDING/待处理、PROCESSING/处理中、COMPLETED/已完成、FAILED/失败")
    private String status;

    @Schema(description = "源链接URL")
    private String sourceUrl;

    @Schema(description = "平台类型")
    private String platform;

    @Schema(description = "检测到的作者列表")
    private List<DetectedAuthor> detectedAuthors;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Data
    @Schema(description = "检测到的作者信息")
    public static class DetectedAuthor {

        @Schema(description = "作者名称")
        private String name;

        @Schema(description = "作者头像URL")
        private String avatarUrl;

        @Schema(description = "置信度 0-1")
        private Double confidence;

        @Schema(description = "主页链接")
        private String homeUrl;

        @Schema(description = "平台类型")
        private String platform;
    }

    @Data
    @Schema(description = "导入成功的信息源")
    public static class ImportedSource {

        @Schema(description = "信息源ID")
        private Long sourceId;

        @Schema(description = "作者名称")
        private String name;

        @Schema(description = "平台类型")
        private String platform;

        @Schema(description = "是否已存在")
        private Boolean existed;
    }

    /**
     * 从实体构建响应
     */
    public static SourceImportJobResponse fromEntity(SourceImportJob job) {
        SourceImportJobResponse response = new SourceImportJobResponse();
        response.setId(job.getId());
        response.setImportType(job.getImportType() != null ? job.getImportType().name() : null);
        response.setStatus(job.getStatus() != null ? job.getStatus().name() : null);
        response.setSourceUrl(job.getSourceUrl());
        response.setPlatform(job.getPlatform());
        response.setErrorMessage(job.getErrorMessage());
        response.setCreatedAt(job.getCreatedAt());
        response.setCompletedAt(job.getCompletedAt());
        response.setRetryCount(job.getRetryCount());
        return response;
    }
}
