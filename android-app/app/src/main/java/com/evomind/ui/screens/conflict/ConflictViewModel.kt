package com.evomind.ui.screens.conflict

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.domain.model.CognitiveConflict
import com.evomind.domain.model.Result
import com.evomind.domain.usecase.AcknowledgeCognitiveConflictUseCase
import com.evomind.domain.usecase.DismissCognitiveConflictUseCase
import com.evomind.domain.usecase.GetUnresolvedConflictsUseCase
import com.evomind.domain.usecase.GetUnresolvedConflictCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 认知冲突 ViewModel
 */
@HiltViewModel
class CognitiveConflictViewModel @Inject constructor(
    private val getUnresolvedConflictsUseCase: GetUnresolvedConflictsUseCase,
    private val acknowledgeConflictUseCase: AcknowledgeCognitiveConflictUseCase,
    private val dismissConflictUseCase: DismissCognitiveConflictUseCase,
    private val getUnresolvedConflictCountUseCase: GetUnresolvedConflictCountUseCase
) : ViewModel() {

    // 冲突列表状态
    private val _conflicts = mutableStateListOf<CognitiveConflict>()
    val conflicts: List<CognitiveConflict> get() = _conflicts

    // 加载状态
    private val _isLoading = mutableStateOf(false)
    val isLoading: Boolean get() = _isLoading.value

    // 错误信息
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: String? get() = _errorMessage.value

    // 未解决冲突数量（用于 Badge）
    private val _unresolvedCount = MutableStateFlow(0)
    val unresolvedCount: StateFlow<Int> = _unresolvedCount

    // 操作成功提示
    private val _successMessage = mutableStateOf<String?>(null)
    val successMessage: String? get() = _successMessage.value

    init {
        loadUnresolvedConflicts()
        loadUnresolvedCount()
    }

    /**
     * 加载未解决的冲突列表
     */
    fun loadUnresolvedConflicts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = getUnresolvedConflictsUseCase()) {
                is Result.Success -> {
                    _conflicts.clear()
                    _conflicts.addAll(result.data)
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
                is Result.Loading -> {
                    // 已在加载中
                }
            }

            _isLoading.value = false
        }
    }

    /**
     * 确认冲突（已阅）
     */
    fun acknowledgeConflict(conflictId: Long) {
        viewModelScope.launch {
            when (val result = acknowledgeConflictUseCase(conflictId)) {
                is Result.Success -> {
                    // 从列表中移除
                    _conflicts.removeAll { it.id == conflictId }
                    _successMessage.value = "已标记为已阅"
                    // 刷新计数
                    loadUnresolvedCount()
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
                is Result.Loading -> {
                    // 加载中
                }
            }
        }
    }

    /**
     * 忽略冲突
     */
    fun dismissConflict(conflictId: Long) {
        viewModelScope.launch {
            when (val result = dismissConflictUseCase(conflictId)) {
                is Result.Success -> {
                    // 从列表中移除
                    _conflicts.removeAll { it.id == conflictId }
                    _successMessage.value = "已忽略该冲突"
                    // 刷新计数
                    loadUnresolvedCount()
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
                is Result.Loading -> {
                    // 加载中
                }
            }
        }
    }

    /**
     * 加载未解决冲突数量
     */
    fun loadUnresolvedCount() {
        viewModelScope.launch {
            when (val result = getUnresolvedConflictCountUseCase()) {
                is Result.Success -> {
                    _unresolvedCount.value = result.data
                }
                is Result.Error -> {
                    // 静默失败，不影响用户
                }
                is Result.Loading -> {
                    // 加载中
                }
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 清除成功提示
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}

/**
 * UI 状态密封类
 */
sealed class CognitiveConflictUiState {
    data object Loading : CognitiveConflictUiState()
    data class Success(val conflicts: List<CognitiveConflict>) : CognitiveConflictUiState()
    data class Error(val message: String) : CognitiveConflictUiState()
}
