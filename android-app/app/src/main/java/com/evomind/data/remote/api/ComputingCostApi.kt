package com.evomind.data.remote.api

import com.evomind.data.remote.dto.response.ApiResponseDto
import com.evomind.data.remote.dto.response.ComputingCostStatsDto
import com.evomind.data.remote.dto.response.CostEstimateDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 算力成本统计API接口
 */
interface ComputingCostApi {

    /**
     * 获取算力成本统计
     */
    @GET("api/v1/computing-cost/stats")
    suspend fun getComputingCostStats(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponseDto<ComputingCostStatsDto>

    /**
     * 预估订阅费用
     */
    @GET("api/v1/computing-cost/estimate")
    suspend fun estimateSubscriptionFee(): ApiResponseDto<CostEstimateDto>
}
