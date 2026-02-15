package com.evomind.ui.screens.payment

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.remote.api.PaymentApi
import com.evomind.data.remote.dto.response.PaymentOrderDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PaymentUiState(
    val isLoading: Boolean = false,
    val orders: List<PaymentOrderDto> = emptyList(),
    val currentOrder: PaymentOrderDto? = null,
    val wechatPayParams: com.evomind.data.remote.dto.response.WechatPayOrderDto? = null,
    val alipayOrderString: String? = null,
    val error: String? = null,
    val paymentSuccess: Boolean = false
)

class PaymentViewModel(
    private val paymentApi: PaymentApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = paymentApi.getOrders()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = response.body()?.data ?: emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load orders"
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

    fun createWechatOrder(planId: Long, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = paymentApi.createWechatOrder(
                    mapOf(
                        "planId" to planId,
                        "amount" to amount
                    )
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        wechatPayParams = response.body()?.data
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to create order"
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

    fun createAlipayOrder(planId: Long, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = paymentApi.createAlipayOrder(
                    mapOf(
                        "planId" to planId,
                        "amount" to amount
                    )
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        alipayOrderString = response.body()?.data?.orderString
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to create order"
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

    fun onPaymentSuccess() {
        _uiState.value = _uiState.value.copy(paymentSuccess = true)
        loadOrders()
    }

    fun clearPaymentParams() {
        _uiState.value = _uiState.value.copy(
            wechatPayParams = null,
            alipayOrderString = null
        )
    }
}
