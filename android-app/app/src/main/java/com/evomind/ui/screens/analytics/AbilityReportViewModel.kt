package com.evomind.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.remote.api.AbilityReportApi
import com.evomind.data.remote.dto.response.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AbilityReportUiState(
    val isLoading: Boolean = false,
    val latestReport: AbilityReportDto? = null,
    val knowledgeGraph: KnowledgeGraphDto? = null,
    val growthCurve: List<GrowthCurvePointDto> = emptyList(),
    val error: String? = null
)

class AbilityReportViewModel(
    private val abilityReportApi: AbilityReportApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(AbilityReportUiState())
    val uiState: StateFlow<AbilityReportUiState> = _uiState.asStateFlow()

    init {
        loadLatestReport()
    }

    fun loadLatestReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val reportResponse = abilityReportApi.getLatestReport()
                val graphResponse = abilityReportApi.getKnowledgeGraph()
                val curveResponse = abilityReportApi.getGrowthCurve()

                if (reportResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        latestReport = reportResponse.body()?.data,
                        knowledgeGraph = graphResponse.body()?.data,
                        growthCurve = curveResponse.body()?.data ?: emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load report"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun loadWeeklyReport(date: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = abilityReportApi.getWeeklyReport(date)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        latestReport = response.body()?.data
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load weekly report"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun loadMonthlyReport(yearMonth: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = abilityReportApi.getMonthlyReport(yearMonth)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        latestReport = response.body()?.data
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load monthly report"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}
