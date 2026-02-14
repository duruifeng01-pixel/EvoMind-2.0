package com.evomind.data.remote.api

import retrofit2.Response
import retrofit2.http.*

/**
 * 语音笔记API接口
 */
interface VoiceNoteApi {

    /**
     * 通过Base64创建语音笔记
     */
    @POST("voice-notes/base64")
    suspend fun createVoiceNoteFromBase64(
        @Body request: VoiceNoteCreateRequestDto
    ): Response<ApiResponse<VoiceNoteDto>>

    /**
     * 获取语音笔记列表
     */
    @GET("voice-notes")
    suspend fun getVoiceNotes(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<VoiceNoteDto>>>

    /**
     * 搜索语音笔记
     */
    @GET("voice-notes/search")
    suspend fun searchVoiceNotes(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<VoiceNoteDto>>>

    /**
     * 获取语音笔记详情
     */
    @GET("voice-notes/{id}")
    suspend fun getVoiceNote(
        @Path("id") id: Long
    ): Response<ApiResponse<VoiceNoteDto>>

    /**
     * 更新语音笔记
     */
    @PUT("voice-notes/{id}")
    suspend fun updateVoiceNote(
        @Path("id") id: Long,
        @Body request: VoiceNoteUpdateRequestDto
    ): Response<ApiResponse<VoiceNoteDto>>

    /**
     * 删除语音笔记
     */
    @DELETE("voice-notes/{id}")
    suspend fun deleteVoiceNote(
        @Path("id") id: Long
    ): Response<ApiResponse<Unit>>

    /**
     * 收藏语音笔记
     */
    @POST("voice-notes/{id}/favorite")
    suspend fun favoriteVoiceNote(
        @Path("id") id: Long
    ): Response<ApiResponse<VoiceNoteDto>>

    /**
     * 取消收藏语音笔记
     */
    @DELETE("voice-notes/{id}/favorite")
    suspend fun unfavoriteVoiceNote(
        @Path("id") id: Long
    ): Response<ApiResponse<VoiceNoteDto>>

    /**
     * 归档语音笔记
     */
    @POST("voice-notes/{id}/archive")
    suspend fun archiveVoiceNote(
        @Path("id") id: Long
    ): Response<ApiResponse<VoiceNoteDto>>

    /**
     * 取消归档语音笔记
     */
    @DELETE("voice-notes/{id}/archive")
    suspend fun unarchiveVoiceNote(
        @Path("id") id: Long
    ): Response<ApiResponse<VoiceNoteDto>>

    /**
     * 重新转写语音笔记
     */
    @POST("voice-notes/{id}/retranscribe")
    suspend fun retranscribe(
        @Path("id") id: Long
    ): Response<ApiResponse<VoiceNoteDto>>

    /**
     * 获取收藏的语音笔记
     */
    @GET("voice-notes/favorites")
    suspend fun getFavoriteVoiceNotes(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<VoiceNoteDto>>>

    /**
     * 获取语音笔记统计
     */
    @GET("voice-notes/statistics")
    suspend fun getStatistics(): Response<ApiResponse<VoiceNoteStatisticsDto>>
}

/**
 * 语音笔记创建请求DTO
 */
data class VoiceNoteCreateRequestDto(
    val audioBase64: String,
    val format: String = "pcm",
    val duration: Int,
    val title: String? = null,
    val tags: String? = null
)

/**
 * 语音笔记更新请求DTO
 */
data class VoiceNoteUpdateRequestDto(
    val title: String? = null,
    val tags: String? = null,
    val transcribedText: String? = null,
    val isFavorite: Boolean? = null,
    val isArchived: Boolean? = null
)

/**
 * 语音笔记响应DTO
 */
data class VoiceNoteDto(
    val id: Long,
    val title: String?,
    val audioUrl: String?,
    val audioDurationSeconds: Int?,
    val audioFormat: String?,
    val fileSizeBytes: Long?,
    val transcribedText: String?,
    val transcribeStatus: String,
    val transcribeError: String?,
    val tags: String?,
    val isFavorite: Boolean,
    val isArchived: Boolean,
    val recordedAt: String?,
    val transcribedAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val isSynced: Boolean
)

/**
 * 语音笔记统计DTO
 */
data class VoiceNoteStatisticsDto(
    val totalCount: Long,
    val todayCount: Long,
    val totalDurationSeconds: Int,
    val pendingTranscribeCount: Long
)

/**
 * 通用API响应包装
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val timestamp: Long
)

/**
 * 分页响应
 */
data class PageResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)
