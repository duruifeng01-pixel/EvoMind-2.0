package com.evomind.domain.model

import java.time.LocalDateTime

/**
 * 语音笔记数据模型
 */
data class VoiceNote(
    val id: Long? = null,
    val title: String? = null,
    val audioUrl: String? = null,
    val audioDurationSeconds: Int? = null,
    val audioFormat: String = "mp3",
    val fileSizeBytes: Long? = null,
    val transcribedText: String? = null,
    val transcribeStatus: TranscribeStatus = TranscribeStatus.PENDING,
    val transcribeError: String? = null,
    val tags: String? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val recordedAt: LocalDateTime? = null,
    val transcribedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val isSynced: Boolean = false
) {
    /**
     * 转写状态枚举
     */
    enum class TranscribeStatus {
        PENDING,      // 待转写
        PROCESSING,   // 转写中
        SUCCESS,      // 转写成功
        FAILED        // 转写失败
    }

    /**
     * 格式化音频时长显示
     */
    fun formatDuration(): String {
        val seconds = audioDurationSeconds ?: 0
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    /**
     * 格式化文件大小
     */
    fun formatFileSize(): String {
        val bytes = fileSizeBytes ?: 0
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * 获取显示标题
     */
    fun getDisplayTitle(): String {
        return when {
            !title.isNullOrBlank() -> title
            !transcribedText.isNullOrBlank() -> {
                if (transcribedText.length > 20) {
                    transcribedText.substring(0, 20) + "..."
                } else {
                    transcribedText
                }
            }
            else -> "语音笔记 ${recordedAt?.toLocalDate() ?: LocalDateTime.now().toLocalDate()}"
        }
    }

    /**
     * 解析标签列表
     */
    fun getTagsList(): List<String> {
        return tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
}

/**
 * 语音笔记创建请求
 */
data class VoiceNoteCreateRequest(
    val audioBase64: String,
    val format: String = "pcm",
    val duration: Int,
    val title: String? = null,
    val tags: String? = null
)

/**
 * 语音笔记更新请求
 */
data class VoiceNoteUpdateRequest(
    val title: String? = null,
    val tags: String? = null,
    val transcribedText: String? = null,
    val isFavorite: Boolean? = null,
    val isArchived: Boolean? = null
)

/**
 * 语音笔记统计信息
 */
data class VoiceNoteStatistics(
    val totalCount: Long,
    val todayCount: Long,
    val totalDurationSeconds: Int,
    val pendingTranscribeCount: Long
) {
    /**
     * 格式化总时长
     */
    fun formatTotalDuration(): String {
        val hours = totalDurationSeconds / 3600
        val minutes = (totalDurationSeconds % 3600) / 60
        return when {
            hours > 0 -> "${hours}小时${minutes}分钟"
            else -> "${minutes}分钟"
        }
    }
}

/**
 * 录音状态
 */
sealed class RecordingState {
    object Idle : RecordingState()
    object Recording : RecordingState()
    data class Processing(val progress: Int = 0) : RecordingState()
    object Success : RecordingState()
    data class Error(val message: String) : RecordingState()
}
