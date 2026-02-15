package com.evomind.ui.screens.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.repository.PrivacyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 隐私与数据权利ViewModel
 */
@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val privacyRepository: PrivacyRepository
) : ViewModel() {

    // 导出状态
    private val _exportUiState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportUiState: StateFlow<ExportUiState> = _exportUiState.asStateFlow()

    // 注销状态
    private val _deletionUiState = MutableStateFlow<DeletionUiState>(DeletionUiState.Idle)
    val deletionUiState: StateFlow<DeletionUiState> = _deletionUiState.asStateFlow()

    private var deletionToken: String? = null

    /**
     * 导出用户数据
     */
    fun exportUserData() {
        viewModelScope.launch {
            _exportUiState.value = ExportUiState.Loading
            privacyRepository.exportUserData().collect { result ->
                result.fold(
                    onSuccess = { data ->
                        val metadata = data["exportMetadata"] as? Map<*, *>
                        val recordCount = metadata?.get("totalRecords") as? Int ?: 0
                        _exportUiState.value = ExportUiState.Success(recordCount)
                    },
                    onFailure = { error ->
                        _exportUiState.value = ExportUiState.Error(error.message ?: "导出失败")
                    }
                )
            }
        }
    }

    /**
     * 申请注销账号
     */
    fun requestAccountDeletion() {
        viewModelScope.launch {
            _deletionUiState.value = DeletionUiState.Loading
            privacyRepository.requestAccountDeletion().collect { result ->
                result.fold(
                    onSuccess = { token ->
                        deletionToken = token
                        _deletionUiState.value = DeletionUiState.Requested
                    },
                    onFailure = { error ->
                        _deletionUiState.value = DeletionUiState.Error(error.message ?: "申请失败")
                    }
                )
            }
        }
    }

    /**
     * 确认注销账号
     */
    fun confirmAccountDeletion() {
        val token = deletionToken ?: return
        viewModelScope.launch {
            _deletionUiState.value = DeletionUiState.Loading
            privacyRepository.confirmAccountDeletion(token).collect { result ->
                result.fold(
                    onSuccess = {
                        _deletionUiState.value = DeletionUiState.Completed
                    },
                    onFailure = { error ->
                        _deletionUiState.value = DeletionUiState.Error(error.message ?: "确认失败")
                    }
                )
            }
        }
    }

    /**
     * 取消注销申请
     */
    fun cancelDeletion() {
        viewModelScope.launch {
            _deletionUiState.value = DeletionUiState.Loading
            privacyRepository.cancelAccountDeletion().collect { result ->
                result.fold(
                    onSuccess = {
                        deletionToken = null
                        _deletionUiState.value = DeletionUiState.Idle
                    },
                    onFailure = { error ->
                        _deletionUiState.value = DeletionUiState.Error(error.message ?: "取消失败")
                    }
                )
            }
        }
    }

    /**
     * 重置导出状态
     */
    fun resetExportState() {
        _exportUiState.value = ExportUiState.Idle
    }

    /**
     * 重置注销状态
     */
    fun resetDeletionState() {
        _deletionUiState.value = DeletionUiState.Idle
        deletionToken = null
    }
}
