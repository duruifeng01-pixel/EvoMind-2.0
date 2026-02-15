package com.evomind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.repository.ChallengeRepository
import com.evomind.domain.model.ChallengeTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengeUiState(
    val isLoading: Boolean = false,
    val currentTask: ChallengeTask? = null,
    val isCompleted: Boolean = false,
    val rewardClaimed: Boolean = false,
    val error: String? = null,
    val showClaimDialog: Boolean = false
)

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentChallenge()
    }

    fun loadCurrentChallenge() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            challengeRepository.getCurrentChallenge().fold(
                onSuccess = { task ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentTask = task,
                            isCompleted = task.isCompleted,
                            rewardClaimed = task.rewardClaimed
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

    fun markAsCompleted() {
        val task = _uiState.value.currentTask ?: return

        viewModelScope.launch {
            challengeRepository.updateChallengeStatus(task.id, true).fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            currentTask = updated,
                            isCompleted = updated.isCompleted,
                            showClaimDialog = updated.isCompleted && !updated.rewardClaimed
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun submitArtifact(title: String, content: String) {
        val task = _uiState.value.currentTask ?: return

        viewModelScope.launch {
            challengeRepository.submitArtifact(
                id = task.id,
                title = title,
                content = content,
                artifactType = "TEXT"
            ).fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            currentTask = updated,
                            isCompleted = updated.isCompleted,
                            showClaimDialog = updated.isCompleted && !updated.rewardClaimed
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun claimReward() {
        val task = _uiState.value.currentTask ?: return

        viewModelScope.launch {
            challengeRepository.claimReward(task.id).fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            currentTask = updated,
                            rewardClaimed = true,
                            showClaimDialog = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun dismissClaimDialog() {
        _uiState.update { it.copy(showClaimDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        const val ACTIVITY_READ_CARD = "READ_CARD"
        const val ACTIVITY_ADD_SOURCE = "ADD_SOURCE"
        const val ACTIVITY_CREATE_NOTE = "CREATE_NOTE"
        const val ACTIVITY_JOIN_DISCUSSION = "JOIN_DISCUSSION"
        const val ACTIVITY_SHARE_INSIGHT = "SHARE_INSIGHT"
        const val ACTIVITY_DAILY_LOGIN = "DAILY_LOGIN"
    }
}
