package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface FreeTrialApi {
    @GET("api/v1/trial/status")
    suspend fun getTrialStatus(): Response<ApiResponseDto<TrialStatusDto>>

    @GET("api/v1/trial/usage")
    suspend fun getUsage(): Response<ApiResponseDto<TrialUsageDto>>

    @POST("api/v1/trial/activate")
    suspend fun activateTrial(): Response<ApiResponseDto<TrialStatusDto>>

    @GET("api/v1/trial/remaining-days")
    suspend fun getRemainingDays(): Response<ApiResponseDto<Int>>
}

data class TrialStatusDto(
    val isActive: Boolean?,
    val startDate: String?,
    val endDate: String?,
    val tier: String?,
    val features: List<String>?
)

data class TrialUsageDto(
    val dailyLimit: Int?,
    val usedToday: Int?,
    val remainingToday: Int?,
    val totalLimit: Int?,
    val usedTotal: Int?
)
