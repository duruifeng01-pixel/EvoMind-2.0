package com.evomind.ui.screens.ocr

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.domain.usecase.OcrResultUiState
import com.evomind.domain.usecase.OcrUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * OCR导入ViewModel
 */
class OcrImportViewModel(
    private val ocrUseCase: OcrUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OcrImportUiState>(OcrImportUiState.Idle)
    val uiState: StateFlow<OcrImportUiState> = _uiState.asStateFlow()

    private var currentImageUri: Uri? = null
    private var currentTaskId: String? = null

    fun onImageSelected(uri: Uri) {
        currentImageUri = uri
        _uiState.value = OcrImportUiState.ImageSelected(uri)
    }

    fun onImageCleared() {
        currentImageUri = null
        currentTaskId = null
        _uiState.value = OcrImportUiState.Idle
    }

    fun startRecognition(
        imageUri: Uri,
        platform: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = OcrImportUiState.Recognizing

            try {
                // 读取图片字节
                val context = getApplicationContext() // 需要实际传入或使用Application
                val bytes = readUriToBytes(context, imageUri)

                ocrUseCase.recognizeBloggers(bytes, platform).collect { state ->
                    when (state) {
                        is OcrResultUiState.Success -> {
                            currentTaskId = state.result.taskId
                            _uiState.value = OcrImportUiState.RecognitionSuccess(state.result)
                            onSuccess(state.result.taskId)
                        }
                        is OcrResultUiState.Error -> {
                            _uiState.value = OcrImportUiState.Error(state.message)
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _uiState.value = OcrImportUiState.Error(e.message ?: "识别失败")
            }
        }
    }

    private suspend fun readUriToBytes(context: Context, uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("无法读取图片")
    }

    private fun getApplicationContext(): Context {
        // 实际项目中应通过Application类获取
        throw NotImplementedError("需要注入Application Context")
    }
}

/**
 * OCR导入UI状态
 */
sealed class OcrImportUiState {
    data object Idle : OcrImportUiState()
    data class ImageSelected(val uri: Uri) : OcrImportUiState()
    data object Recognizing : OcrImportUiState()
    data class RecognitionSuccess(val result: com.evomind.domain.model.OcrResult) : OcrImportUiState()
    data class Error(val message: String) : OcrImportUiState()
}
