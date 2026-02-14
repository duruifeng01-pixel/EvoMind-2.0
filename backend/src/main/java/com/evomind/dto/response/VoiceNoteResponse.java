package com.evomind.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 语音笔记响应DTO
 */
@Data
public class VoiceNoteResponse {

    private Long id;

    private String title;

    private String audioUrl;

    private Integer audioDurationSeconds;

    private String audioFormat;

    private Long fileSizeBytes;

    private String transcribedText;

    private String transcribeStatus;

    private String transcribeError;

    private String tags;

    private Boolean isFavorite;

    private Boolean isArchived;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transcribedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Boolean isSynced;

    /**
     * 转写状态枚举
     */
    public enum TranscribeStatus {
        PENDING,      // 待转写
        PROCESSING,   // 转写中
        SUCCESS,      // 转写成功
        FAILED        // 转写失败
    }
}
