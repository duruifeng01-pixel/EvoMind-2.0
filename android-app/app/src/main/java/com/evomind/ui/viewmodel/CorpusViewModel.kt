package com.evomind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.repository.CorpusRepository
import com.evomind.domain.model.CorpusStats
import com.evomind.domain.model.PagedResult
import com.evomind.domain.model.UserCorpus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 语料库ViewModel状态
 */
data class CorpusUiState(
    val isLoading: Boolean = false,
    val corpusList: List<UserCorpus> = emptyList(),
    val stats: CorpusStats? = null,
    val selectedTab: CorpusTab = CorpusTab.ALL,
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val error: String? = null
)

enum class CorpusTab {
    ALL,        // 全部
    FAVORITES,  // 收藏
    ARCHIVED,   // 归档
    TYPE_SOCRATIC,    // 苏格拉底洞察
    TYPE_NOTE,        // 用户笔记
    TYPE_AI_SUMMARY,  // AI总结
    TYPE_REFLECTION   // 反思记录
}

@HiltViewModel
class CorpusViewModel @Inject constructor(
    private val corpusRepository: CorpusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CorpusUiState())
    val uiState: StateFlow<CorpusUiState> = _uiState.asStateFlow()

    init {
        loadCorpus()
        loadStats()
    }

    fun loadCorpus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = when (_uiState.value.selectedTab) {
                CorpusTab.ALL -> corpusRepository.getUserCorpus(0, 20)
                CorpusTab.FAVORITES -> corpusRepository.getFavoriteCorpus(0, 20)
                CorpusTab.ARCHIVED -> corpusRepository.getArchivedCorpus(0, 20)
                CorpusTab.TYPE_SOCRATIC -> corpusRepository.getCorpusByType("SOCRATIC_INSIGHT", 0, 20)
                CorpusTab.TYPE_NOTE -> corpusRepository.getCorpusByType("USER_NOTE", 0, 20)
                CorpusTab.TYPE_AI_SUMMARY -> corpusRepository.getCorpusByType("AI_SUMMARY", 0, 20)
                CorpusTab.TYPE_REFLECTION -> corpusRepository.getCorpusByType("REFLECTION", 0, 20)
            }
            
            result.fold(
                onSuccess = { pagedResult ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            corpusList = pagedResult.items,
                            currentPage = 0,
                            hasMore = pagedResult.hasMore
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "加载失败"
                        )
                    }
                }
            )
        }
    }

    fun loadMore() {
        if (!_uiState.value.hasMore || _uiState.value.isLoading) return
        
        viewModelScope.launch {
            val nextPage = _uiState.value.currentPage + 1
            
            val result = when (_uiState.value.selectedTab) {
                CorpusTab.ALL -> corpusRepository.getUserCorpus(nextPage, 20)
                CorpusTab.FAVORITES -> corpusRepository.getFavoriteCorpus(nextPage, 20)
                CorpusTab.ARCHIVED -> corpusRepository.getArchivedCorpus(nextPage, 20)
                CorpusTab.TYPE_SOCRATIC -> corpusRepository.getCorpusByType("SOCRATIC_INSIGHT", nextPage, 20)
                CorpusTab.TYPE_NOTE -> corpusRepository.getCorpusByType("USER_NOTE", nextPage, 20)
                CorpusTab.TYPE_AI_SUMMARY -> corpusRepository.getCorpusByType("AI_SUMMARY", nextPage, 20)
                CorpusTab.TYPE_REFLECTION -> corpusRepository.getCorpusByType("REFLECTION", nextPage, 20)
            }
            
            result.fold(
                onSuccess = { pagedResult ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            corpusList = it.corpusList + pagedResult.items,
                            currentPage = nextPage,
                            hasMore = pagedResult.hasMore
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "加载更多失败"
                        )
                    }
                }
            )
        }
    }

    fun searchCorpus(keyword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            corpusRepository.searchCorpus(keyword, 0, 20).fold(
                onSuccess = { pagedResult ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            corpusList = pagedResult.items,
                            currentPage = 0,
                            hasMore = pagedResult.hasMore
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "搜索失败"
                        )
                    }
                }
            )
        }
    }

    fun selectTab(tab: CorpusTab) {
        _uiState.update { it.copy(selectedTab = tab, corpusList = emptyList()) }
        loadCorpus()
    }

    fun loadStats() {
        viewModelScope.launch {
            corpusRepository.getCorpusStats().fold(
                onSuccess = { stats ->
                    _uiState.update { it.copy(stats = stats) }
                },
                onFailure = { /* ignore */ }
            )
        }
    }

    fun toggleFavorite(corpusId: Long) {
        viewModelScope.launch {
            corpusRepository.toggleFavorite(corpusId).fold(
                onSuccess = { updated ->
                    _uiState.update { state ->
                        state.copy(
                            corpusList = state.corpusList.map { 
                                if (it.id == corpusId) updated else it 
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun togglePin(corpusId: Long) {
        viewModelScope.launch {
            corpusRepository.togglePin(corpusId).fold(
                onSuccess = { updated ->
                    _uiState.update { state ->
                        state.copy(
                            corpusList = state.corpusList.map { 
                                if (it.id == corpusId) updated else it 
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun archiveCorpus(corpusId: Long) {
        viewModelScope.launch {
            corpusRepository.archiveCorpus(corpusId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            corpusList = state.corpusList.filter { it.id != corpusId }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun deleteCorpus(corpusId: Long) {
        viewModelScope.launch {
            corpusRepository.deleteCorpus(corpusId).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            corpusList = state.corpusList.filter { it.id != corpusId }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
