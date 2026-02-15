package com.evomind.data.remote.dto.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 算力成本统计响应DTO
 */
data class ComputingCostStatsDto(
    @SerializedName("startDate")
    val startDate: String,

    @SerializedName("endDate")
    val endDate: String,

    @SerializedName("days")
    val days: Int,

    @SerializedName("metrics")
    val metrics: MetricsDto,

    @SerializedName("costAmount")
    val costAmount: CostAmountDto,

    @SerializedName("subscription")
    val subscription: SubscriptionDto,

    @SerializedName("dailyRecords")
    val dailyRecords: List<DailyRecordDto>,

    @SerializedName("formulaDescription")
    val formulaDescription: String
)

data class MetricsDto(
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
    val feedCardCount: Int = 0,

    @SerializedName("crawlRequestCount")
    val crawlRequestCount: Int = 0
)

data class CostAmountDto(
    @SerializedName("ocrCost")
    val ocrCost: BigDecimal = BigDecimal.ZERO,

    @SerializedName("aiCost")
    val aiCost: BigDecimal = BigDecimal.ZERO,

    @SerializedName("crawlCost")
    val crawlCost: BigDecimal = BigDecimal.ZERO,

    @SerializedName("storageCost")
    val storageCost: BigDecimal = BigDecimal.ZERO,

    @SerializedName("totalCost")
    val totalCost: BigDecimal = BigDecimal.ZERO
)

data class SubscriptionDto(
    @SerializedName("costAmount")
    val costAmount: BigDecimal = BigDecimal.ZERO,           // 展示算力成本（80%）

    @SerializedName("operationCost")
    val operationCost: BigDecimal = BigDecimal.ZERO,        // 展示运营成本（20%）

    @SerializedName("totalSubscriptionFee")
    val totalSubscriptionFee: BigDecimal = BigDecimal.ZERO, // 总订阅费

    @SerializedName("costMultiplier")
    val costMultiplier: Int = 80,  // 算力成本占比80%

    @SerializedName("pricingModel")
    val pricingModel: String = ""
)

data class DailyRecordDto(
    @SerializedName("date")
    val date: String,

    @SerializedName("totalCost")
    val totalCost: BigDecimal = BigDecimal.ZERO,

    @SerializedName("subscriptionFee")
    val subscriptionFee: BigDecimal = BigDecimal.ZERO,

    @SerializedName("sourceCount")
    val sourceCount: Int = 0,

    @SerializedName("aiTokenCount")
    val aiTokenCount: Long = 0L
)
