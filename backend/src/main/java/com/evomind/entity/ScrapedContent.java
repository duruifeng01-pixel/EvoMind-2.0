package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 抓取内容实体
 * 存储从各平台抓取的文章/视频内容
 */
@Entity
@Table(name = "scraped_contents")
@Getter
@Setter
public class ScrapedContent extends BaseEntity {

    @Column(name = "task_id", nullable = false, length = 64, unique = true)
    private String taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source_url", nullable = false, length = 2048)
    private String sourceUrl;

    @Column(name = "platform", length = 20)
    private String platform;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "author", length = 100)
    private String author;

    @Column(name = "author_avatar", length = 512)
    private String authorAvatar;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "summary", length = 2000)
    private String summary;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "images_json", columnDefinition = "TEXT")
    private String imagesJson;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private ScrapeStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Column(name = "word_count")
    private Integer wordCount;

    public enum ScrapeStatus {
        PENDING,        // 待处理
        SCRAPING,       // 抓取中
        SUCCESS,        // 抓取成功
        FAILED,         // 抓取失败
        RETRYING        // 重试中
    }

    public void startScraping() {
        this.status = ScrapeStatus.SCRAPING;
    }

    public void complete() {
        this.status = ScrapeStatus.SUCCESS;
    }

    public void fail(String error) {
        this.status = ScrapeStatus.FAILED;
        this.errorMessage = error;
    }

    public void retry() {
        this.status = ScrapeStatus.RETRYING;
        this.retryCount++;
    }
}
