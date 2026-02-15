package com.evomind.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.remote.api.GrowthStatsApi
import com.evomind.data.remote.dto.response.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GrowthStatsUiState(
    val isLoading: Boolean = false,
    val growthStats: GrowthStatsDto? = null,
    val abilityProfile: AbilityProfileDto? = null,
    val evolutionProgress: EvolutionProgressDto? = null,
    val error: String? = null
)

class GrowthStatsViewModel(
    private val growthStatsApi: GrowthStatsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrowthStatsUiState())
    val uiState: StateFlow<GrowthStatsUiState> = _uiState.asStateFlow()

    init {
        loadGrowthStats()
    }

    fun loadGrowthStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val statsResponse = growthStatsApi.getGrowthStats()
                val abilityResponse = growthStatsApi.getAbilityProfile()
                val evolutionResponse = growthStatsApi.getEvolutionProgress()

                if (statsResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        growthStats = statsResponse.body()?.data,
                        abilityProfile = abilityResponse.body()?.data,
                        evolutionProgress = evolutionResponse.body()?.data
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load stats"
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
