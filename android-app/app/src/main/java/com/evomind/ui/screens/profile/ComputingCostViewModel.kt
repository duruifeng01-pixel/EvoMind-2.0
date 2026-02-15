package com.evomind.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.remote.RetrofitClient
import com.evomind.data.remote.api.ComputingCostApi
import com.evomind.data.remote.dto.response.CostEstimateDto
import com.evomind.data.remote.dto.response.UnitPriceDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * 算力成本统计 ViewModel
 */
class ComputingCostViewModel : ViewModel() {

    private val computingCostApi = RetrofitClient.createService(ComputingCostApi::class.java)

    private val _uiState = MutableStateFlow(ComputingCostUiState())
    val uiState: StateFlow<ComputingCostUiState> = _uiState.asStateFlow()

    init {
        loadCostStats()
        loadCostEstimate()
    }

    private fun loadCostStats() {
        viewModelScope.launch {
            try {
                val response = computingCostApi.getComputingCostStats()
                if (response.code == 200 && response.data != null) {
                    val stats = response.data
                    _uiState.update { state ->
                        state.copy(
                            totalCost = stats.costAmount.totalCost,
                            ocrCost = stats.costAmount.ocrCost,
                            aiCost = stats.costAmount.aiCost,
                            crawlCost = stats.costAmount.crawlCost,
                            storageCost = stats.costAmount.storageCost,
                            sourceCount = stats.metrics.sourceCount,
                            ocrRequestCount = stats.metrics.ocrRequestCount,
                            aiTokenCount = stats.metrics.aiTokenCount,
                            dialogueTurnCount = stats.metrics.dialogueTurnCount,
                            conflictMarkCount = stats.metrics.conflictMarkCount,
                            costMultiplier = stats.subscription.costMultiplier,
                            formulaDescription = stats.formulaDescription,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadCostEstimate() {
        viewModelScope.launch {
            try {
                val response = computingCostApi.estimateSubscriptionFee()
                if (response.code == 200 && response.data != null) {
                    val estimate = response.data
                    _uiState.update { state ->
                        state.copy(
                            monthlyEstimate = estimate.estimate.monthlySubscription,
                            unitPrices = estimate.unitPrices.map { it.toUnitPriceItem() },
                            costMultiplier = estimate.estimate.costMultiplier
                        )
                    }
                }
            } catch (e: Exception) {
                // 静默处理，使用默认值
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadCostStats()
        loadCostEstimate()
    }
}

/**
 * UI 状态
 */
data class ComputingCostUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // 成本金额
    val totalCost: BigDecimal = BigDecimal.ZERO,
    val ocrCost: BigDecimal = BigDecimal.ZERO,
    val aiCost: BigDecimal = BigDecimal.ZERO,
    val crawlCost: BigDecimal = BigDecimal.ZERO,
    val storageCost: BigDecimal = BigDecimal.ZERO,

    // 使用统计
    val sourceCount: Int = 0,
    val ocrRequestCount: Int = 0,
    val aiTokenCount: Long = 0L,
    val dialogueTurnCount: Int = 0,
    val conflictMarkCount: Int = 0,

    // 订阅信息
    val monthlyEstimate: BigDecimal = BigDecimal.ZERO,
    val costMultiplier: Int = 2,
    val formulaDescription: String = "订阅费 = 算力成本 × 2",

    // 单价列表
    val unitPrices: List<UnitPriceItem> = emptyList()
)

// 转换函数
private fun UnitPriceDto.toUnitPriceItem(): UnitPriceItem {
    return UnitPriceItem(
        name = this.name,
        price = this.price,
        unit = this.unit
    )
}
