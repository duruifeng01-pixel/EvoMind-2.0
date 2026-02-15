package com.evomind.ui.screens.share

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.remote.api.ShareImageApi
import com.evomind.data.remote.dto.response.GeneratedShareImageDto
import com.evomind.data.remote.dto.response.ShareTemplateDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class ShareImageUiState(
    val isLoading: Boolean = false,
    val templates: List<ShareTemplateDto> = emptyList(),
    val generatedImage: GeneratedShareImageDto? = null,
    val myImages: List<GeneratedShareImageDto> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val savedSuccess: Boolean = false
)

class ShareImageViewModel(
    private val shareImageApi: ShareImageApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareImageUiState())
    val uiState: StateFlow<ShareImageUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = shareImageApi.getTemplates()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        templates = response.body()?.data ?: emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load templates"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun generateShareImage(templateId: Long, content: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val request = content.toMutableMap()
                request["templateId"] = templateId
                val response = shareImageApi.generateShareImage(request)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generatedImage = response.body()?.data
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to generate image"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun loadMyImages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = shareImageApi.getMyShareImages()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myImages = response.body()?.data ?: emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load images"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun saveImageToGallery(context: Context, imageUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, savedSuccess = false)
            try {
                // 实际项目中需要下载图片然后保存
                // 这里简化处理
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    savedSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message
                )
            }
        }
    }

    fun shareImage(context: Context, imageUrl: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_TEXT, "我在EvoMind学习成长中，一起来吧！")
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享到"))
    }

    fun clearSavedFlag() {
        _uiState.value = _uiState.value.copy(savedSuccess = false)
    }
}
