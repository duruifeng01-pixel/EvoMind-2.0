package com.evomind.data.remote.dto.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * 订阅费用预估响应DTO
 */
data class CostEstimateDto(
    @SerializedName("estimateBasedOn")
    val estimateBasedOn: String,

    @SerializedName("currentUsage")
    val currentUsage: CurrentUsageDto,

    @SerializedName("estimate")
    val estimate: EstimateResultDto,

    @SerializedName("unitPrices")
    val unitPrices: List<UnitPriceDto>,

    @SerializedName("formula")
    val formula: String
)

data class CurrentUsageDto(
    @SerializedName("sourceCount")
    val sourceCount: Int = 0,

    @SerializedName("conflictMarkCount")
    val conflictMarkCount: Int = 0,

    @SerializedName("ocrRequestCount")
    val ocrRequestCount: Int = 0,

    @SerializedName("aiTokenCount")
    val aiTokenCount: Long = 0L,

    @SerializedName("dialogueTurnCount")
    val dialogueTurnCount: Int = 0,

    @SerializedName("modelTrainingCount")
    val modelTrainingCount: Int = 0,

    @SerializedName("feedCardCount")
    val feedCardCount: Int = 0
)

data class EstimateResultDto(
    @SerializedName("dailyCost")
    val dailyCost: BigDecimal = BigDecimal.ZERO,        // 日算力成本

    @SerializedName("monthlyCost")
    val monthlyCost: BigDecimal = BigDecimal.ZERO,      // 月算力成本

    @SerializedName("dailySubscription")
    val dailySubscription: BigDecimal = BigDecimal.ZERO,    // 日订阅费

    @SerializedName("monthlySubscription")
    val monthlySubscription: BigDecimal = BigDecimal.ZERO,  // 月订阅费

    @SerializedName("computingCostRatio")
    val computingCostRatio: Int = 80,   // 算力成本占比80%

    @SerializedName("operationCostRatio")
    val operationCostRatio: Int = 20    // 运营成本占比20%
)

data class UnitPriceDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("price")
    val price: BigDecimal = BigDecimal.ZERO,

    @SerializedName("unit")
    val unit: String,

    @SerializedName("category")
    val category: String
)
