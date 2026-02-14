package com.evomind.ui.screens.ocr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.domain.model.OcrResult
import com.evomind.domain.usecase.ConfirmImportUiState
import com.evomind.domain.usecase.OcrResultUiState
import com.evomind.domain.usecase.OcrUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * OCR结果ViewModel
 */
class OcrResultViewModel(
    private val ocrUseCase: OcrUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OcrResultUiState>(OcrResultUiState.Loading)
    val uiState: StateFlow<OcrResultUiState> = _uiState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private var currentResult: OcrResult? = null

    fun loadResult(taskId: String) {
        viewModelScope.launch {
            _uiState.value = OcrResultUiState.Loading

            ocrUseCase.getImportLimit().collect { state ->
                // 先检查限额
            }

            // 获取识别结果
            // 这里简化处理，实际应从API获取结果
            // 由于识别已经在OcrImportScreen完成，这里直接从本地状态或重新获取
            val result = ocrUseCase.recognizeBloggers(byteArrayOf(), null) // 简化处理
                .first { it is OcrResultUiState.Success } as? OcrResultUiState.Success

            if (result != null) {
                currentResult = result.result
                _uiState.value = OcrResultUiState.Success(result.result)
            } else {
                _uiState.value = OcrResultUiState.Error("获取识别结果失败")
            }
        }
    }

    fun confirmImport(taskId: String, selectedIds: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading

            ocrUseCase.confirmImport(taskId, selectedIds).collect { state ->
                when (state) {
                    is ConfirmImportUiState.Success -> {
                        _importState.value = ImportState.Success(state.results.size)
                        onComplete()
                    }
                    is ConfirmImportUiState.Error -> {
                        _importState.value = ImportState.Error(state.message)
                    }
                    else -> {}
                }
            }
        }
    }
}

/**
 * 导入状态
 */
sealed class ImportState {
    data object Idle : ImportState()
    data object Loading : ImportState()
    data class Success(val importedCount: Int) : ImportState()
    data class Error(val message: String) : ImportState()
}
