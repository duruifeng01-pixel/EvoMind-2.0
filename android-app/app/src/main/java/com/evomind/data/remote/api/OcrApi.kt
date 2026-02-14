package com.evomind.data.remote.api

import com.evomind.domain.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * OCR相关API接口
 */
interface OcrApi {
    
    /**
     * OCR截图识别导入
     */
    @POST("sources/ocr-import")
    suspend fun ocrImport(
        @Body request: OcrImportRequestDto
    ): Response<ApiResponse<OcrResultDto>>
    
    /**
     * 获取OCR识别结果
     */
    @GET("sources/ocr-result/{taskId}")
    suspend fun getOcrResult(
        @Path("taskId") taskId: String
    ): Response<ApiResponse<OcrResultDto>>
    
    /**
     * 确认导入OCR识别结果
     */
    @POST("sources/confirm-import")
    suspend fun confirmImport(
        @Body request: ConfirmImportRequestDto
    ): Response<ApiResponse<List<ImportResultDto>>>
    
    /**
     * 查询今日导入限额
     */
    @GET("sources/import-limit")
    suspend fun getImportLimit(): Response<ApiResponse<ImportLimitDto>>
}

/**
 * API响应包装类
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val success: Boolean
)

/**
 * DTO数据传输对象
 */
data class OcrImportRequestDto(
    val imageBase64: String,
    val imageFormat: String = "jpeg",
    val platform: String? = null,
    val preprocessImage: Boolean = true,
    val imageWidth: Int? = null,
    val imageHeight: Int? = null
)

data class OcrResultDto(
    val taskId: String,
    val bloggers: List<DetectedBloggerDto>?,
    val textBlocks: List<TextBlockDto>?,
    val status: String,
    val processingTimeMs: Long?,
    val recognizedAt: String?,
    val needsConfirmation: Boolean?
)

data class DetectedBloggerDto(
    val candidateId: String,
    val name: String,
    val avatarUrl: String?,
    val platform: String,
    val homeUrl: String?,
    val confidence: Double,
    val alreadyExists: Boolean?,
    val existingSourceId: Long?,
    val boundingBox: BoundingBoxDto?
)

data class TextBlockDto(
    val text: String,
    val boundingBox: BoundingBoxDto?,
    val confidence: Double?
)

data class BoundingBoxDto(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

data class ConfirmImportRequestDto(
    val taskId: String,
    val selectedCandidateIds: List<String>,
    val selectAll: Boolean = false,
    val customNames: Map<String, String>? = null
)

data class ImportResultDto(
    val sourceId: Long?,
    val name: String,
    val platform: String,
    val existed: Boolean?
)

data class ImportLimitDto(
    val dailyLimit: Int,
    val isExceeded: Boolean,
    val remaining: Int
)
