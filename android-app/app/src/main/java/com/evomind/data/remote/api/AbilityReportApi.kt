package com.evomind.data.remote.api

import com.evomind.data.remote.dto.ApiResponseDto
import com.evomind.data.remote.dto.response.*
import retrofit2.Response
import retrofit2.http.*

interface AbilityReportApi {
    @GET("api/v1/ability-report/latest")
    suspend fun getLatestReport(): Response<ApiResponseDto<AbilityReportDto>>

    @GET("api/v1/ability-report/weekly")
    suspend fun getWeeklyReport(
        @Query("date") date: String? = null
    ): Response<ApiResponseDto<AbilityReportDto>>

    @GET("api/v1/ability-report/monthly")
    suspend fun getMonthlyReport(
        @Query("yearMonth") yearMonth: String? = null
    ): Response<ApiResponseDto<AbilityReportDto>>

    @GET("api/v1/ability-report/history")
    suspend fun getReportHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 12
    ): Response<ApiResponseDto<List<AbilityReportDto>>>

    @GET("api/v1/ability-report/{id}/pdf")
    suspend fun getPdfUrl(@Path("id") id: Long): Response<ApiResponseDto<String>>

    @GET("api/v1/ability-report/knowledge-graph")
    suspend fun getKnowledgeGraph(): Response<ApiResponseDto<KnowledgeGraphDto>>

    @GET("api/v1/ability-report/growth-curve")
    suspend fun getGrowthCurve(
        @Query("period") period: String = "monthly"
    ): Response<ApiResponseDto<List<GrowthCurvePointDto>>>
}
