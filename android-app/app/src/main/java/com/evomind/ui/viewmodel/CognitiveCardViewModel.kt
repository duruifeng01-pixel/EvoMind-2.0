package com.evomind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.repository.CardRepository
import com.evomind.domain.model.Card
import com.evomind.domain.model.MindMap
import com.evomind.domain.model.DrilldownContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CognitiveCardUiState(
    val isLoading: Boolean = false,
    val card: Card? = null,
    val mindMap: MindMap? = null,
    val error: String? = null,
    val isFavorite: Boolean = false
)

@HiltViewModel
class CognitiveCardViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CognitiveCardUiState())
    val uiState: StateFlow<CognitiveCardUiState> = _uiState.asStateFlow()

    fun loadCard(cardId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            cardRepository.getCard(cardId).fold(
                onSuccess = { card ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            card = card,
                            isFavorite = card.isFavorite
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

    fun toggleFavorite() {
        val cardId = _uiState.value.card?.id ?: return

        viewModelScope.launch {
            cardRepository.toggleFavorite(cardId).fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            card = updated,
                            isFavorite = updated.isFavorite
                        )
                    }
                },
                onFailure = { /* ignore */ }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
