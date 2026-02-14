package com.evomind.domain.model

import java.time.LocalDateTime

/**
 * OCR识别结果数据模型
 */
data class OcrResult(
    val taskId: String,
    val bloggers: List<DetectedBlogger>,
    val textBlocks: List<TextBlock> = emptyList(),
    val status: String,
    val processingTimeMs: Long? = null,
    val recognizedAt: LocalDateTime? = null,
    val needsConfirmation: Boolean = true
)

/**
 * 检测到的博主信息
 */
data class DetectedBlogger(
    val candidateId: String,
    val name: String,
    val avatarUrl: String? = null,
    val platform: String,
    val homeUrl: String? = null,
    val confidence: Double,
    val alreadyExists: Boolean = false,
    val existingSourceId: Long? = null,
    val boundingBox: BoundingBox? = null
)

/**
 * 文本块信息
 */
data class TextBlock(
    val text: String,
    val boundingBox: BoundingBox? = null,
    val confidence: Double? = null
)

/**
 * 边界框位置信息
 */
data class BoundingBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

/**
 * OCR导入请求
 */
data class OcrImportRequest(
    val imageBase64: String,
    val imageFormat: String = "jpeg",
    val platform: String? = null,
    val preprocessImage: Boolean = true,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null
) {
    fun getImageDataUrl(): String {
        val format = imageFormat.lowercase().let { if (it == "jpg") "jpeg" else it }
        return "data:image/$format;base64,$imageBase64"
    }
}

/**
 * 确认导入请求
 */
data class ConfirmImportRequest(
    val taskId: String,
    val selectedCandidateIds: List<String>,
    val selectAll: Boolean = false,
    val customNames: Map<String, String>? = null
)

/**
 * 导入结果
 */
data class ImportResult(
    val sourceId: Long? = null,
    val name: String,
    val platform: String,
    val existed: Boolean = false
)

/**
 * 导入限额信息
 */
data class ImportLimit(
    val dailyLimit: Int,
    val isExceeded: Boolean,
    val remaining: Int
)
