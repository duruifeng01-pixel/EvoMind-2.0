package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface GrowthStatsApi {
    @GET("api/v1/growth/stats")
    suspend fun getGrowthStats(): Response<ApiResponseDto<GrowthStatsDto>>

    @GET("api/v1/growth/stats/weekly")
    suspend fun getWeeklyStats(): Response<ApiResponseDto<WeeklyStatsDto>>

    @GET("api/v1/growth/stats/monthly")
    suspend fun getMonthlyStats(): Response<ApiResponseDto<MonthlyStatsDto>>

    @GET("api/v1/growth/ability-profile")
    suspend fun getAbilityProfile(): Response<ApiResponseDto<AbilityProfileDto>>

    @GET("api/v1/growth/evolution-progress")
    suspend fun getEvolutionProgress(): Response<ApiResponseDto<EvolutionProgressDto>>
}
