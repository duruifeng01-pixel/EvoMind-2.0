package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * OCR导入日志实体
 * 记录OCR识别请求和结果
 */
@Entity
@Table(name = "ocr_import_logs")
@Getter
@Setter
public class OcrImportLog extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_id", nullable = false, length = 64, unique = true)
    private String taskId;

    @Column(name = "platform", length = 20)
    private String platform;

    @Column(name = "image_hash", length = 64)
    private String imageHash;

    @Column(name = "original_image_url", length = 512)
    private String originalImageUrl;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "raw_result", columnDefinition = "TEXT")
    private String rawResult;

    @Column(name = "detected_count")
    private Integer detectedCount;

    @Column(name = "imported_count")
    private Integer importedCount;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private OcrStatus status;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    public enum OcrStatus {
        PROCESSING,     // 处理中
        SUCCESS,        // 识别成功待确认
        CONFIRMED,      // 已确认导入
        FAILED          // 识别失败
    }
}
