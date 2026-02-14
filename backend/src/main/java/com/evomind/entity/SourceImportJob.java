package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 信息源导入任务实体
 * 用于跟踪OCR识别和链接抓取任务
 */
@Entity
@Table(name = "source_import_jobs")
@Getter
@Setter
public class SourceImportJob extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ImportType importType;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "platform", length = 50)
    private String platform;

    @Column(name = "detected_authors", columnDefinition = "TEXT")
    private String detectedAuthorsJson;

    @Column(name = "selected_authors", columnDefinition = "TEXT")
    private String selectedAuthorsJson;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    public enum ImportType {
        OCR_SCREENSHOT,  // OCR截图识别
        LINK_SCRAPE      // 链接抓取
    }

    public enum JobStatus {
        PENDING,         // 待处理
        PROCESSING,      // 处理中
        COMPLETED,       // 已完成
        FAILED,          // 失败
        RETRYING         // 重试中
    }

    public void startProcessing() {
        this.status = JobStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = JobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String error) {
        this.status = JobStatus.FAILED;
        this.errorMessage = error;
        this.completedAt = LocalDateTime.now();
    }

    public void retry() {
        this.status = JobStatus.RETRYING;
        this.retryCount++;
    }
}
