package com.evomind.ui.screens.conflict

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.domain.model.CardConflict
import com.evomind.domain.repository.ConflictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 冲突检测ViewModel
 */
@HiltViewModel
class ConflictViewModel @Inject constructor(
    private val conflictRepository: ConflictRepository
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow(ConflictUiState())
    val uiState: StateFlow<ConflictUiState> = _uiState.asStateFlow()

    // 未确认冲突数量（用于Badge）
    val unresolvedCount = conflictRepository.observeUnresolvedConflictCount()

    init {
        loadUnresolvedConflicts()
    }

    /**
     * 加载未确认冲突
     */
    fun loadUnresolvedConflicts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            conflictRepository.getUnresolvedConflicts()
                .onSuccess { conflicts ->
                    _uiState.value = _uiState.value.copy(
                        conflicts = conflicts,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    /**
     * 检测指定卡片的冲突
     */
    fun detectConflicts(cardId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            conflictRepository.detectConflicts(cardId)
                .onSuccess { conflicts ->
                    _uiState.value = _uiState.value.copy(
                        detectedConflicts = conflicts,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    /**
     * 获取卡片相关冲突
     */
    fun getConflictsByCard(cardId: Long) {
        viewModelScope.launch {
            conflictRepository.getConflictsByCard(cardId)
                .onSuccess { conflicts ->
                    _uiState.value = _uiState.value.copy(
                        cardConflicts = conflicts,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message
                    )
                }
        }
    }

    /**
     * 确认冲突
     */
    fun acknowledgeConflict(conflictId: Long) {
        viewModelScope.launch {
            conflictRepository.acknowledgeConflict(conflictId)
                .onSuccess {
                    // 从列表中移除已确认的冲突
                    val updatedConflicts = _uiState.value.conflicts.filter { it.id != conflictId }
                    _uiState.value = _uiState.value.copy(
                        conflicts = updatedConflicts
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message
                    )
                }
        }
    }

    /**
     * 刷新未确认冲突数量
     */
    fun refreshUnresolvedCount() {
        viewModelScope.launch {
            conflictRepository.getUnresolvedConflictCount()
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 冲突UI状态
 */
data class ConflictUiState(
    val conflicts: List<CardConflict> = emptyList(),
    val detectedConflicts: List<CardConflict> = emptyList(),
    val cardConflicts: List<CardConflict> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
