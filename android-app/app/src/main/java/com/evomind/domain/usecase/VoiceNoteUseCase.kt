package com.evomind.domain.usecase

import com.evomind.domain.model.*
import com.evomind.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 语音笔记UseCase
 */
class VoiceNoteUseCase(
    private val voiceNoteRepository: VoiceNoteRepository
) {

    /**
     * 创建语音笔记
     */
    suspend fun createVoiceNote(
        audioBase64: String,
        duration: Int,
        format: String = "pcm",
        title: String? = null,
        tags: String? = null
    ): Flow<VoiceNoteUiState> = flow {
        emit(VoiceNoteUiState.Loading)

        val request = VoiceNoteCreateRequest(
            audioBase64 = audioBase64,
            format = format,
            duration = duration,
            title = title,
            tags = tags
        )

        val result = voiceNoteRepository.createVoiceNote(request)

        result.fold(
            onSuccess = { emit(VoiceNoteUiState.Success(it)) },
            onFailure = { emit(VoiceNoteUiState.Error(it.message ?: "创建失败")) }
        )
    }

    /**
     * 获取语音笔记列表
     */
    suspend fun getVoiceNotes(page: Int = 0, size: Int = 20): Flow<VoiceNoteListUiState> = flow {
        emit(VoiceNoteListUiState.Loading)

        val result = voiceNoteRepository.getVoiceNotes(page, size)

        result.fold(
            onSuccess = { emit(VoiceNoteListUiState.Success(it)) },
            onFailure = { emit(VoiceNoteListUiState.Error(it.message ?: "获取失败")) }
        )
    }

    /**
     * 搜索语音笔记
     */
    suspend fun searchVoiceNotes(keyword: String, page: Int = 0, size: Int = 20): Flow<VoiceNoteListUiState> = flow {
        emit(VoiceNoteListUiState.Loading)

        val result = voiceNoteRepository.searchVoiceNotes(keyword, page, size)

        result.fold(
            onSuccess = { emit(VoiceNoteListUiState.Success(it)) },
            onFailure = { emit(VoiceNoteListUiState.Error(it.message ?: "搜索失败")) }
        )
    }

    /**
     * 获取语音笔记详情
     */
    suspend fun getVoiceNote(id: Long): Flow<VoiceNoteUiState> = flow {
        emit(VoiceNoteUiState.Loading)

        val result = voiceNoteRepository.getVoiceNote(id)

        result.fold(
            onSuccess = { emit(VoiceNoteUiState.Success(it)) },
            onFailure = { emit(VoiceNoteUiState.Error(it.message ?: "获取失败")) }
        )
    }

    /**
     * 更新语音笔记
     */
    suspend fun updateVoiceNote(
        id: Long,
        title: String? = null,
        tags: String? = null,
        transcribedText: String? = null
    ): Flow<VoiceNoteUiState> = flow {
        emit(VoiceNoteUiState.Loading)

        val request = VoiceNoteUpdateRequest(
            title = title,
            tags = tags,
            transcribedText = transcribedText
        )

        val result = voiceNoteRepository.updateVoiceNote(id, request)

        result.fold(
            onSuccess = { emit(VoiceNoteUiState.Success(it)) },
            onFailure = { emit(VoiceNoteUiState.Error(it.message ?: "更新失败")) }
        )
    }

    /**
     * 删除语音笔记
     */
    suspend fun deleteVoiceNote(id: Long): Flow<DeleteUiState> = flow {
        emit(DeleteUiState.Loading)

        val result = voiceNoteRepository.deleteVoiceNote(id)

        result.fold(
            onSuccess = { emit(DeleteUiState.Success) },
            onFailure = { emit(DeleteUiState.Error(it.message ?: "删除失败")) }
        )
    }

    /**
     * 收藏/取消收藏
     */
    suspend fun toggleFavorite(id: Long, favorite: Boolean): Flow<VoiceNoteUiState> = flow {
        emit(VoiceNoteUiState.Loading)

        val result = voiceNoteRepository.toggleFavorite(id, favorite)

        result.fold(
            onSuccess = { emit(VoiceNoteUiState.Success(it)) },
            onFailure = { emit(VoiceNoteUiState.Error(it.message ?: "操作失败")) }
        )
    }

    /**
     * 获取统计信息
     */
    suspend fun getStatistics(): Flow<StatisticsUiState> = flow {
        emit(StatisticsUiState.Loading)

        val result = voiceNoteRepository.getStatistics()

        result.fold(
            onSuccess = { emit(StatisticsUiState.Success(it)) },
            onFailure = { emit(StatisticsUiState.Error(it.message ?: "获取统计失败")) }
        )
    }
}

/**
 * 语音笔记UI状态
 */
sealed class VoiceNoteUiState {
    object Loading : VoiceNoteUiState()
    data class Success(val voiceNote: VoiceNote) : VoiceNoteUiState()
    data class Error(val message: String) : VoiceNoteUiState()
}

/**
 * 语音笔记列表UI状态
 */
sealed class VoiceNoteListUiState {
    object Loading : VoiceNoteListUiState()
    data class Success(val voiceNotes: List<VoiceNote>) : VoiceNoteListUiState()
    data class Error(val message: String) : VoiceNoteListUiState()
}

/**
 * 删除UI状态
 */
sealed class DeleteUiState {
    object Loading : DeleteUiState()
    object Success : DeleteUiState()
    data class Error(val message: String) : DeleteUiState()
}

/**
 * 统计UI状态
 */
sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val statistics: VoiceNoteStatistics) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}
