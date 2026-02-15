package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface FeedApi {
    @GET("api/v1/cards/feed")
    suspend fun getFeed(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("type") type: String? = null
    ): Response<ApiResponseDto<FeedResponseDto>>

    @GET("api/v1/cards/feed/stats")
    suspend fun getFeedStats(): Response<ApiResponseDto<FeedStatsDto>>

    @GET("api/v1/cards/{id}")
    suspend fun getCard(@Path("id") id: Long): Response<ApiResponseDto<Any>>

    @POST("api/v1/cards/{id}/read")
    suspend fun markAsRead(@Path("id") id: Long): Response<ApiResponseDto<Unit>>

    @POST("api/v1/cards/{id}/archive")
    suspend fun archiveCard(@Path("id") id: Long): Response<ApiResponseDto<Any>>
}
