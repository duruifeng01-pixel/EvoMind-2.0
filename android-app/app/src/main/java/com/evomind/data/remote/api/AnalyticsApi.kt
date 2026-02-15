package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface AnalyticsApi {
    @GET("api/v1/analytics/app-start")
    suspend fun trackAppStart(): Response<ApiResponseDto<Unit>>

    @GET("api/v1/analytics/screen-view")
    suspend fun trackScreenView(@Query("screen") screen: String): Response<ApiResponseDto<Unit>>

    @GET("api/v1/analytics/feature-use")
    suspend fun trackFeatureUse(@Query("feature") feature: String): Response<ApiResponseDto<Unit>>

    @GET("api/v1/analytics/performance")
    suspend fun reportPerformance(@Body metrics: Map<String, Any>): Response<ApiResponseDto<Unit>>

    @GET("api/v1/analytics/crash")
    suspend fun reportCrash(@Body crash: Map<String, Any>): Response<ApiResponseDto<Unit>>
}
