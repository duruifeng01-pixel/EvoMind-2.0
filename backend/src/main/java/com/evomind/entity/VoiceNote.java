package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 语音笔记实体类
 * 存储用户语音记录的元数据和转写文本
 */
@Entity
@Table(name = "voice_notes")
@Getter
@Setter
public class VoiceNote extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "audio_url", length = 512)
    private String audioUrl;

    @Column(name = "audio_duration_seconds")
    private Integer audioDurationSeconds;

    @Column(name = "audio_format", length = 10)
    private String audioFormat = "mp3";

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "transcribed_text", length = 4000)
    private String transcribedText;

    @Column(name = "transcribe_status", length = 20)
    private String transcribeStatus = "PENDING";

    @Column(name = "transcribe_error")
    private String transcribeError;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "tags")
    private String tags;

    @Column(name = "is_favorite")
    private Boolean isFavorite = false;

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Column(name = "transcribed_at")
    private LocalDateTime transcribedAt;

    @Column(name = "local_path", length = 512)
    private String localPath;

    @Column(name = "is_synced")
    private Boolean isSynced = false;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @PrePersist
    public void prePersist() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
