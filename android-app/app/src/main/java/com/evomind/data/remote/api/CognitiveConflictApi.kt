package com.evomind.data.remote.api

import com.evomind.data.remote.dto.response.CognitiveConflictResponseDto
import com.evomind.data.remote.dto.response.UserCognitiveProfileDto
import com.evomind.data.remote.dto.response.UnresolvedConflictsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 认知冲突 API 接口
 */
interface CognitiveConflictApi {

    /**
     * 检测卡片与认知画像的冲突
     */
    @POST("/cognitive-conflicts/detect/{cardId}")
    suspend fun detectConflict(
        @Path("cardId") cardId: Long
    ): Response<CognitiveConflictResponseDto>

    /**
     * 获取未解决的冲突列表
     */
    @GET("/cognitive-conflicts/unresolved")
    suspend fun getUnresolvedConflicts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<UnresolvedConflictsResponseDto>

    /**
     * 确认冲突（已阅并标记）
     */
    @POST("/cognitive-conflicts/{id}/acknowledge")
    suspend fun acknowledgeConflict(
        @Path("id") conflictId: Long
    ): Response<Void>

    /**
     * 忽略冲突
     */
    @POST("/cognitive-conflicts/{id}/dismiss")
    suspend fun dismissConflict(
        @Path("id") conflictId: Long
    ): Response<Void>

    /**
     * 获取用户认知画像列表
     */
    @GET("/cognitive-conflicts/profiles")
    suspend fun getCognitiveProfiles(): Response<List<UserCognitiveProfileDto>>

    /**
     * 重建认知画像
     */
    @POST("/cognitive-conflicts/profiles/rebuild")
    suspend fun rebuildProfiles(): Response<Void>
}
