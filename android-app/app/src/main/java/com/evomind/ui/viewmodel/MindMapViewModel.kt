package com.evomind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.repository.MindMapRepository
import com.evomind.domain.model.DrilldownContent
import com.evomind.domain.model.MindMap
import com.evomind.domain.model.MindMapNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MindMapUiState(
    val isLoading: Boolean = false,
    val mindMap: MindMap? = null,
    val selectedNode: MindMapNode? = null,
    val drilldownContent: DrilldownContent? = null,
    val expandedNodes: Set<String> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class MindMapViewModel @Inject constructor(
    private val mindMapRepository: MindMapRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MindMapUiState())
    val uiState: StateFlow<MindMapUiState> = _uiState.asStateFlow()

    fun loadMindMap(cardId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            mindMapRepository.getMindMap(cardId).fold(
                onSuccess = { mindMap ->
                    val expandedNodes = mindMap.nodes.map { it.nodeId }.toSet()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mindMap = mindMap,
                            expandedNodes = expandedNodes
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

    fun toggleNode(nodeId: String) {
        _uiState.update { state ->
            val newExpanded = if (nodeId in state.expandedNodes) {
                state.expandedNodes - nodeId
            } else {
                state.expandedNodes + nodeId
            }
            state.copy(expandedNodes = newExpanded)
        }
    }

    fun selectNode(node: MindMapNode) {
        _uiState.update { it.copy(selectedNode = node) }
        
        if (node.hasOriginalReference && node.originalContentId != null) {
            loadDrilldown(node)
        }
    }

    private fun loadDrilldown(node: MindMapNode) {
        val cardId = _uiState.value.mindMap?.cardId ?: return
        val nodeId = node.nodeId

        viewModelScope.launch {
            mindMapRepository.getDrilldown(cardId, nodeId).fold(
                onSuccess = { content ->
                    _uiState.update { it.copy(drilldownContent = content) }
                },
                onFailure = { /* ignore */ }
            )
        }
    }

    fun clearDrilldown() {
        _uiState.update { it.copy(drilldownContent = null, selectedNode = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
