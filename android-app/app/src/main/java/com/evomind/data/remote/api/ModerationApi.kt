package com.evomind.data.remote.api

import com.evomind.data.model.ModerationRequest
import com.evomind.data.model.ModerationResponse
import com.evomind.data.model.QuickCheckRequest
import com.evomind.data.model.QuickCheckResponse
import com.evomind.data.model.ModerationStatistics
import com.evomind.data.remote.dto.ApiResponse
import com.evomind.data.remote.dto.PagedResponse
import com.evomind.data.model.ModerationLogItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 内容审核API接口
 */
interface ModerationApi {

    /**
     * 内容审核（同步）
     */
    @POST("api/v1/moderation/check")
    suspend fun moderateContent(
        @Body request: ModerationRequest
    ): ApiResponse<ModerationResponse>

    /**
     * 快速敏感词检测
     */
    @POST("api/v1/moderation/quick-check")
    suspend fun quickCheck(
        @Body request: QuickCheckRequest
    ): ApiResponse<QuickCheckResponse>

    /**
     * AI生成内容审核
     */
    @POST("api/v1/moderation/ai-content")
    suspend fun moderateAiContent(
        @Body request: Map<String, String>
    ): ApiResponse<ModerationResponse>

    /**
     * 获取审核历史
     */
    @GET("api/v1/moderation/history")
    suspend fun getModerationHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PagedResponse<ModerationLogItem>>

    /**
     * 获取审核统计
     */
    @GET("api/v1/moderation/statistics")
    suspend fun getModerationStatistics(): ApiResponse<ModerationStatistics>
}