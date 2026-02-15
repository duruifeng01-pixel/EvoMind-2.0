package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface ConflictApi {
    @GET("api/v1/conflicts")
    suspend fun getConflicts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponseDto<List<ConflictDto>>>

    @GET("api/v1/conflicts/{id}")
    suspend fun getConflict(@Path("id") id: Long): Response<ApiResponseDto<ConflictDto>>

    @GET("api/v1/conflicts/{id}/comparison")
    suspend fun getComparison(@Path("id") id: Long): Response<ApiResponseDto<ConflictComparisonDto>>

    @POST("api/v1/conflicts/{id}/dismiss")
    suspend fun dismissConflict(@Path("id") id: Long): Response<ApiResponseDto<Unit>>
}
