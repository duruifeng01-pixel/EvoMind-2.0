package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.CardResponseDto
import com.evomind.data.remote.dto.response.MindMapResponseDto
import com.evomind.data.remote.dto.response.DrilldownResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CardApi {
    @GET("api/v1/cards/feed")
    suspend fun getFeed(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<List<CardResponseDto>>>

    @GET("api/v1/cards/{id}")
    suspend fun getCard(
        @Path("id") cardId: Long
    ): Response<ApiResponseDto<CardResponseDto>>

    @GET("api/v1/cards/{id}/mindmap")
    suspend fun getMindMap(
        @Path("id") cardId: Long
    ): Response<ApiResponseDto<MindMapResponseDto>>

    @GET("api/v1/cards/{id}/drilldown")
    suspend fun getDrilldown(
        @Path("id") cardId: Long,
        @Query("nodeId") nodeId: String
    ): Response<ApiResponseDto<DrilldownResponseDto>>

    @POST("api/v1/cards/{id}/favorite")
    suspend fun toggleFavorite(
        @Path("id") cardId: Long
    ): Response<ApiResponseDto<CardResponseDto>>
}
