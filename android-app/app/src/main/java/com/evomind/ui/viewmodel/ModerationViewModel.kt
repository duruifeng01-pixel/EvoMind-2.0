package com.evomind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.model.ContentType
import com.evomind.data.model.ModerationRequest
import com.evomind.data.model.ModerationResponse
import com.evomind.data.model.ModerationStatistics
import com.evomind.data.model.ModerationLogItem
import com.evomind.data.remote.dto.PagedResponse
import com.evomind.data.repository.ModerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 内容审核ViewModel
 */
@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val moderationRepository: ModerationRepository
) : ViewModel() {

    // 审核状态
    private val _moderationState = MutableStateFlow<ModerationUiState>(ModerationUiState.Idle)
    val moderationState: StateFlow<ModerationUiState> = _moderationState.asStateFlow()

    // 快速检测状态
    private val _quickCheckState = MutableStateFlow<QuickCheckUiState>(QuickCheckUiState.Idle)
    val quickCheckState: StateFlow<QuickCheckUiState> = _quickCheckState.asStateFlow()

    // 审核历史
    private val _historyState = MutableStateFlow<HistoryUiState>(HistoryUiState.Idle)
    val historyState: StateFlow<HistoryUiState> = _historyState.asStateFlow()

    // 审核统计
    private val _statisticsState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Idle)
    val statisticsState: StateFlow<StatisticsUiState> = _statisticsState.asStateFlow()

    /**
     * 审核内容
     */
    fun moderateContent(
        content: String,
        contentType: ContentType,
        contentId: String? = null,
        isAiGenerated: Boolean = false,
        aiModel: String? = null
    ) {
        viewModelScope.launch {
            _moderationState.value = ModerationUiState.Loading

            val request = ModerationRequest(
                contentType = contentType,
                contentId = contentId,
                content = content,
                isAiGenerated = isAiGenerated,
                aiModel = aiModel
            )

            moderationRepository.moderateContent(request)
                .onSuccess { response ->
                    _moderationState.value = ModerationUiState.Success(response)
                }
                .onFailure { error ->
                    _moderationState.value = ModerationUiState.Error(error.message ?: "审核失败")
                }
        }
    }

    /**
     * 快速检测敏感词
     */
    fun quickCheck(content: String) {
        viewModelScope.launch {
            _quickCheckState.value = QuickCheckUiState.Loading

            moderationRepository.quickCheck(content)
                .onSuccess { response ->
                    _quickCheckState.value = QuickCheckUiState.Success(response)
                }
                .onFailure { error ->
                    _quickCheckState.value = QuickCheckUiState.Error(error.message ?: "检测失败")
                }
        }
    }

    /**
     * AI生成内容审核
     */
    fun moderateAiContent(content: String, contentType: ContentType, aiModel: String? = null) {
        viewModelScope.launch {
            _moderationState.value = ModerationUiState.Loading

            moderationRepository.moderateAiContent(
                content = content,
                contentType = contentType.name,
                aiModel = aiModel
            )
                .onSuccess { response ->
                    _moderationState.value = ModerationUiState.Success(response)
                }
                .onFailure { error ->
                    _moderationState.value = ModerationUiState.Error(error.message ?: "审核失败")
                }
        }
    }

    /**
     * 获取审核历史
     */
    fun loadHistory(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            _historyState.value = HistoryUiState.Loading

            moderationRepository.getModerationHistory(page, size)
                .onSuccess { response ->
                    _historyState.value = HistoryUiState.Success(response)
                }
                .onFailure { error ->
                    _historyState.value = HistoryUiState.Error(error.message ?: "获取历史失败")
                }
        }
    }

    /**
     * 获取审核统计
     */
    fun loadStatistics() {
        viewModelScope.launch {
            _statisticsState.value = StatisticsUiState.Loading

            moderationRepository.getModerationStatistics()
                .onSuccess { stats ->
                    _statisticsState.value = StatisticsUiState.Success(stats)
                }
                .onFailure { error ->
                    _statisticsState.value = StatisticsUiState.Error(error.message ?: "获取统计失败")
                }
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _moderationState.value = ModerationUiState.Idle
        _quickCheckState.value = QuickCheckUiState.Idle
    }

    // ==================== UI状态 ====================

    sealed class ModerationUiState {
        object Idle : ModerationUiState()
        object Loading : ModerationUiState()
        data class Success(val response: ModerationResponse) : ModerationUiState()
        data class Error(val message: String) : ModerationUiState()
    }

    sealed class QuickCheckUiState {
        object Idle : QuickCheckUiState()
        object Loading : QuickCheckUiState()
        data class Success(val hasSensitiveWord: Boolean, val hitWords: List<String>?) : QuickCheckUiState()
        data class Error(val message: String) : QuickCheckUiState()
    }

    sealed class HistoryUiState {
        object Idle : HistoryUiState()
        object Loading : HistoryUiState()
        data class Success(val data: PagedResponse<ModerationLogItem>) : HistoryUiState()
        data class Error(val message: String) : HistoryUiState()
    }

    sealed class StatisticsUiState {
        object Idle : StatisticsUiState()
        object Loading : StatisticsUiState()
        data class Success(val statistics: ModerationStatistics) : StatisticsUiState()
        data class Error(val message: String) : StatisticsUiState()
    }
}