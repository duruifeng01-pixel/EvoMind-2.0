package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface UserWorkApi {
    @GET("api/v1/works")
    suspend fun getWorks(
        @Query("page") page: Int = 0,
        @Query("type") type: String? = null
    ): Response<ApiResponseDto<List<UserWorkDto>>>

    @GET("api/v1/works/{id}")
    suspend fun getWork(@Path("id") id: Long): Response<ApiResponseDto<UserWorkDto>>

    @POST("api/v1/works")
    suspend fun createWork(@Body request: Map<String, Any>): Response<ApiResponseDto<UserWorkDto>>

    @DELETE("api/v1/works/{id}")
    suspend fun deleteWork(@Path("id") id: Long): Response<ApiResponseDto<Unit>>

    @GET("api/v1/works/{id}/badge")
    suspend fun getBadge(@Path("id") id: Long): Response<ApiResponseDto<BadgeDto>>
}
