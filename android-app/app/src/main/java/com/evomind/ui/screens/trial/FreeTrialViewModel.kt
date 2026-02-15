package com.evomind.ui.screens.trial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.remote.api.FreeTrialApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FreeTrialUiState(
    val isLoading: Boolean = false,
    val isActive: Boolean = false,
    val remainingDays: Int = 0,
    val dailyLimit: Int = 0,
    val usedToday: Int = 0,
    val remainingToday: Int = 0,
    val error: String? = null
)

class FreeTrialViewModel(
    private val freeTrialApi: FreeTrialApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(FreeTrialUiState())
    val uiState: StateFlow<FreeTrialUiState> = _uiState.asStateFlow()

    init {
        loadTrialStatus()
    }

    fun loadTrialStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val statusResponse = freeTrialApi.getTrialStatus()
                val usageResponse = freeTrialApi.getUsage()

                if (statusResponse.isSuccessful && usageResponse.isSuccessful) {
                    val status = statusResponse.body()?.data
                    val usage = usageResponse.body()?.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isActive = status?.isActive ?: false,
                        remainingDays = freeTrialApi.getRemainingDays().body()?.data ?: 0,
                        dailyLimit = usage?.dailyLimit ?: 0,
                        usedToday = usage?.usedToday ?: 0,
                        remainingToday = usage?.remainingToday ?: 0
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load trial status"
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

    fun activateTrial() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = freeTrialApi.activateTrial()
                if (response.isSuccessful) {
                    loadTrialStatus()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to activate trial"
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
