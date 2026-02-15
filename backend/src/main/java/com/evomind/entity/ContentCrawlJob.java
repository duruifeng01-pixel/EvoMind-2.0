package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 内容采集任务实体
 * 记录每次采集任务的执行状态和结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "content_crawl_jobs")
public class ContentCrawlJob extends BaseEntity {

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "platform", length = 50)
    private String platform;

    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    @Column(name = "job_type", length = 20)
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "articles_found")
    private Integer articlesFound = 0;

    @Column(name = "articles_new")
    private Integer articlesNew = 0;

    @Column(name = "articles_duplicated")
    private Integer articlesDuplicated = 0;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    public ContentCrawlJob() {
        this.status = JobStatus.PENDING;
        this.jobType = JobType.SCHEDULED;
    }

    public enum JobType {
        SCHEDULED,      // 定时任务
        MANUAL,         // 手动触发
        DISCOVERY       // 内容发现
    }

    public enum JobStatus {
        PENDING,        // 待执行
        RUNNING,        // 执行中
        COMPLETED,      // 完成
        FAILED,         // 失败
        CANCELLED       // 取消
    }

    /**
     * 获取执行时长（秒）
     */
    public Long getDurationSeconds() {
        if (startedAt == null || completedAt == null) return 0L;
        return java.time.Duration.between(startedAt, completedAt).getSeconds();
    }
}
