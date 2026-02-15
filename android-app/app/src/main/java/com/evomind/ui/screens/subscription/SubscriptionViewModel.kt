package com.evomind.ui.screens.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.remote.api.SubscriptionApi
import com.evomind.data.remote.dto.response.SubscriptionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val plans: List<SubscriptionDto> = emptyList(),
    val currentSubscription: SubscriptionDto? = null,
    val error: String? = null
)

class SubscriptionViewModel(
    private val subscriptionApi: SubscriptionApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = subscriptionApi.getPlans()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        plans = response.body()?.data ?: emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load plans"
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

    fun loadCurrentSubscription() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = subscriptionApi.getCurrentSubscription()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentSubscription = response.body()?.data
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load subscription"
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

    fun subscribe(planId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = subscriptionApi.subscribe(mapOf("planId" to planId))
                if (response.isSuccessful) {
                    loadCurrentSubscription()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to subscribe"
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
