package com.evomind.data.repository

import com.evomind.data.remote.api.*
import com.evomind.domain.model.*
import com.evomind.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 语音笔记Repository实现
 */
@Singleton
class VoiceNoteRepositoryImpl @Inject constructor(
    private val voiceNoteApi: VoiceNoteApi
) : VoiceNoteRepository {

    private val _localVoiceNotes = MutableStateFlow<List<VoiceNote>>(emptyList())

    override suspend fun createVoiceNote(request: VoiceNoteCreateRequest): Result<VoiceNote> {
        return try {
            val dtoRequest = VoiceNoteCreateRequestDto(
                audioBase64 = request.audioBase64,
                format = request.format,
                duration = request.duration,
                title = request.title,
                tags = request.tags
            )
            val response = voiceNoteApi.createVoiceNoteFromBase64(dtoRequest)
            if (response.isSuccessful && response.body()?.data != null) {
                val voiceNote = response.body()!!.data!!.toDomainModel()
                Result.success(voiceNote)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "创建失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVoiceNotes(page: Int, size: Int): Result<List<VoiceNote>> {
        return try {
            val response = voiceNoteApi.getVoiceNotes(page, size)
            if (response.isSuccessful && response.body()?.data != null) {
                val voiceNotes = response.body()!!.data!!.content.map { it.toDomainModel() }
                Result.success(voiceNotes)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "获取失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchVoiceNotes(keyword: String, page: Int, size: Int): Result<List<VoiceNote>> {
        return try {
            val response = voiceNoteApi.searchVoiceNotes(keyword, page, size)
            if (response.isSuccessful && response.body()?.data != null) {
                val voiceNotes = response.body()!!.data!!.content.map { it.toDomainModel() }
                Result.success(voiceNotes)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "搜索失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVoiceNote(id: Long): Result<VoiceNote> {
        return try {
            val response = voiceNoteApi.getVoiceNote(id)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.toDomainModel())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "获取失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVoiceNote(id: Long, request: VoiceNoteUpdateRequest): Result<VoiceNote> {
        return try {
            val dtoRequest = VoiceNoteUpdateRequestDto(
                title = request.title,
                tags = request.tags,
                transcribedText = request.transcribedText,
                isFavorite = request.isFavorite,
                isArchived = request.isArchived
            )
            val response = voiceNoteApi.updateVoiceNote(id, dtoRequest)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.toDomainModel())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "更新失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVoiceNote(id: Long): Result<Boolean> {
        return try {
            val response = voiceNoteApi.deleteVoiceNote(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "删除失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(id: Long, favorite: Boolean): Result<VoiceNote> {
        return try {
            val response = if (favorite) {
                voiceNoteApi.favoriteVoiceNote(id)
            } else {
                voiceNoteApi.unfavoriteVoiceNote(id)
            }
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.toDomainModel())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "操作失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleArchive(id: Long, archive: Boolean): Result<VoiceNote> {
        return try {
            val response = if (archive) {
                voiceNoteApi.archiveVoiceNote(id)
            } else {
                voiceNoteApi.unarchiveVoiceNote(id)
            }
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.toDomainModel())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "操作失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun retranscribe(id: Long): Result<VoiceNote> {
        return try {
            val response = voiceNoteApi.retranscribe(id)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.toDomainModel())
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "重新转写失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFavoriteVoiceNotes(page: Int, size: Int): Result<List<VoiceNote>> {
        return try {
            val response = voiceNoteApi.getFavoriteVoiceNotes(page, size)
            if (response.isSuccessful && response.body()?.data != null) {
                val voiceNotes = response.body()!!.data!!.content.map { it.toDomainModel() }
                Result.success(voiceNotes)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "获取失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStatistics(): Result<VoiceNoteStatistics> {
        return try {
            val response = voiceNoteApi.getStatistics()
            if (response.isSuccessful && response.body()?.data != null) {
                val dto = response.body()!!.data!!
                Result.success(VoiceNoteStatistics(
                    totalCount = dto.totalCount,
                    todayCount = dto.todayCount,
                    totalDurationSeconds = dto.totalDurationSeconds,
                    pendingTranscribeCount = dto.pendingTranscribeCount
                ))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "获取统计失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLocalVoiceNotesFlow(): Flow<List<VoiceNote>> {
        return _localVoiceNotes.asStateFlow()
    }

    override suspend fun syncVoiceNotes(): Result<Unit> {
        // TODO: 实现本地数据同步逻辑
        return Result.success(Unit)
    }

    /**
     * DTO转Domain Model
     */
    private fun VoiceNoteDto.toDomainModel(): VoiceNote {
        return VoiceNote(
            id = this.id,
            title = this.title,
            audioUrl = this.audioUrl,
            audioDurationSeconds = this.audioDurationSeconds,
            audioFormat = this.audioFormat ?: "mp3",
            fileSizeBytes = this.fileSizeBytes,
            transcribedText = this.transcribedText,
            transcribeStatus = VoiceNote.TranscribeStatus.valueOf(this.transcribeStatus),
            transcribeError = this.transcribeError,
            tags = this.tags,
            isFavorite = this.isFavorite,
            isArchived = this.isArchived,
            recordedAt = this.recordedAt?.let { parseDateTime(it) },
            transcribedAt = this.transcribedAt?.let { parseDateTime(it) },
            createdAt = this.createdAt?.let { parseDateTime(it) },
            updatedAt = this.updatedAt?.let { parseDateTime(it) },
            isSynced = this.isSynced
        )
    }

    private fun parseDateTime(dateTimeStr: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            null
        }
    }
}
