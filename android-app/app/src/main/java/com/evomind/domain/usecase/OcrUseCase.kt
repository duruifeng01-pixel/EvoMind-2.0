package com.evomind.domain.usecase

import com.evomind.domain.model.*
import com.evomind.domain.repository.OcrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * OCR识别UseCase
 */
class OcrUseCase(
    private val ocrRepository: OcrRepository
) {
    /**
     * 执行OCR识别
     */
    suspend fun recognizeBloggers(imageBytes: ByteArray, platform: String? = null): Flow<OcrResultUiState> = flow {
        emit(OcrResultUiState.Loading)

        // 压缩图片
        val compressedBytes = ocrRepository.compressImage(imageBytes, maxSizeKB = 1024)

        // 转换为Base64
        val base64 = ocrRepository.encodeImageToBase64(compressedBytes)

        // 创建请求
        val request = OcrImportRequest(
            imageBase64 = base64,
            platform = platform,
            preprocessImage = true
        )

        // 调用API
        val result = ocrRepository.recognizeBloggers(request)

        result.fold(
            onSuccess = { emit(OcrResultUiState.Success(it)) },
            onFailure = { emit(OcrResultUiState.Error(it.message ?: "识别失败")) }
        )
    }

    /**
     * 确认导入
     */
    suspend fun confirmImport(
        taskId: String,
        selectedIds: List<String>,
        selectAll: Boolean = false
    ): Flow<ConfirmImportUiState> = flow {
        emit(ConfirmImportUiState.Loading)

        val request = ConfirmImportRequest(
            taskId = taskId,
            selectedCandidateIds = selectedIds,
            selectAll = selectAll
        )

        val result = ocrRepository.confirmImport(request)

        result.fold(
            onSuccess = { emit(ConfirmImportUiState.Success(it)) },
            onFailure = { emit(ConfirmImportUiState.Error(it.message ?: "导入失败")) }
        )
    }

    /**
     * 获取导入限额
     */
    suspend fun getImportLimit(): Flow<ImportLimitUiState> = flow {
        emit(ImportLimitUiState.Loading)

        val result = ocrRepository.getImportLimit()

        result.fold(
            onSuccess = { emit(ImportLimitUiState.Success(it)) },
            onFailure = { emit(ImportLimitUiState.Error(it.message ?: "获取限额失败")) }
        )
    }
}

/**
 * OCR结果UI状态
 */
sealed class OcrResultUiState {
    data object Loading : OcrResultUiState()
    data class Success(val result: OcrResult) : OcrResultUiState()
    data class Error(val message: String) : OcrResultUiState()
}

/**
 * 确认导入UI状态
 */
sealed class ConfirmImportUiState {
    data object Loading : ConfirmImportUiState()
    data class Success(val results: List<ImportResult>) : ConfirmImportUiState()
    data class Error(val message: String) : ConfirmImportUiState()
}

/**
 * 导入限额UI状态
 */
sealed class ImportLimitUiState {
    data object Loading : ImportLimitUiState()
    data class Success(val limit: ImportLimit) : ImportLimitUiState()
    data class Error(val message: String) : ImportLimitUiState()
}
