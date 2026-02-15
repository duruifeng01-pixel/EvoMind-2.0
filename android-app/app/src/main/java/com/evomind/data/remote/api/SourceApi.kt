package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface SourceApi {
    @GET("api/v1/sources")
    suspend fun getSources(): Response<ApiResponseDto<List<SourceDto>>>

    @GET("api/v1/sources/{id}")
    suspend fun getSource(@Path("id") id: Long): Response<ApiResponseDto<SourceDto>>

    @POST("api/v1/sources")
    suspend fun addSource(@Body request: Map<String, Any>): Response<ApiResponseDto<SourceDto>>

    @PUT("api/v1/sources/{id}")
    suspend fun updateSource(
        @Path("id") id: Long,
        @Body request: Map<String, Any>
    ): Response<ApiResponseDto<SourceDto>>

    @DELETE("api/v1/sources/{id}")
    suspend fun deleteSource(@Path("id") id: Long): Response<ApiResponseDto<Unit>>

    @POST("api/v1/sources/{id}/refresh")
    suspend fun refreshSource(@Path("id") id: Long): Response<ApiResponseDto<SourceDto>>

    @GET("api/v1/sources/{id}/stats")
    suspend fun getSourceStats(@Path("id") id: Long): Response<ApiResponseDto<SourceStatsDto>>
}
