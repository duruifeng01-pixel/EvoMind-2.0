package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ConflictResponseDto
import com.evomind.data.remote.dto.ApiResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 冲突检测API接口
 */
interface ConflictApi {

    /**
     * 检测卡片冲突
     */
    @POST("/api/v1/conflicts/detect/{cardId}")
    suspend fun detectConflicts(
        @Path("cardId") cardId: Long
    ): ApiResponseDto<List<ConflictResponseDto>>

    /**
     * 获取未确认冲突
     */
    @GET("/api/v1/conflicts/unresolved")
    suspend fun getUnresolvedConflicts(): ApiResponseDto<List<ConflictResponseDto>>

    /**
     * 获取卡片相关冲突
     */
    @GET("/api/v1/conflicts/card/{cardId}")
    suspend fun getConflictsByCard(
        @Path("cardId") cardId: Long
    ): ApiResponseDto<List<ConflictResponseDto>>

    /**
     * 确认冲突
     */
    @POST("/api/v1/conflicts/{conflictId}/acknowledge")
    suspend fun acknowledgeConflict(
        @Path("conflictId") conflictId: Long
    ): ApiResponseDto<Unit>

    /**
     * 检查卡片间冲突
     */
    @GET("/api/v1/conflicts/check")
    suspend fun checkConflictBetween(
        @Query("cardId1") cardId1: Long,
        @Query("cardId2") cardId2: Long
    ): ApiResponseDto<Boolean>

    /**
     * 获取未确认冲突数量
     */
    @GET("/api/v1/conflicts/count/unresolved")
    suspend fun getUnresolvedConflictCount(): ApiResponseDto<Long>
}
