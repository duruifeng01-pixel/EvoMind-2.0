package com.evomind.domain.repository

import com.evomind.domain.model.VoiceNote
import com.evomind.domain.model.VoiceNoteCreateRequest
import com.evomind.domain.model.VoiceNoteStatistics
import com.evomind.domain.model.VoiceNoteUpdateRequest
import kotlinx.coroutines.flow.Flow

/**
 * 语音笔记Repository接口
 */
interface VoiceNoteRepository {

    /**
     * 创建语音笔记
     * @param request 创建请求
     * @return 创建的语音笔记
     */
    suspend fun createVoiceNote(request: VoiceNoteCreateRequest): Result<VoiceNote>

    /**
     * 获取语音笔记列表
     * @param page 页码
     * @param size 每页数量
     * @return 语音笔记列表
     */
    suspend fun getVoiceNotes(page: Int = 0, size: Int = 20): Result<List<VoiceNote>>

    /**
     * 搜索语音笔记
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @return 搜索结果
     */
    suspend fun searchVoiceNotes(keyword: String, page: Int = 0, size: Int = 20): Result<List<VoiceNote>>

    /**
     * 获取语音笔记详情
     * @param id 笔记ID
     * @return 语音笔记详情
     */
    suspend fun getVoiceNote(id: Long): Result<VoiceNote>

    /**
     * 更新语音笔记
     * @param id 笔记ID
     * @param request 更新请求
     * @return 更新后的语音笔记
     */
    suspend fun updateVoiceNote(id: Long, request: VoiceNoteUpdateRequest): Result<VoiceNote>

    /**
     * 删除语音笔记
     * @param id 笔记ID
     * @return 是否成功
     */
    suspend fun deleteVoiceNote(id: Long): Result<Boolean>

    /**
     * 收藏/取消收藏语音笔记
     * @param id 笔记ID
     * @param favorite 是否收藏
     * @return 更新后的语音笔记
     */
    suspend fun toggleFavorite(id: Long, favorite: Boolean): Result<VoiceNote>

    /**
     * 归档/取消归档语音笔记
     * @param id 笔记ID
     * @param archive 是否归档
     * @return 更新后的语音笔记
     */
    suspend fun toggleArchive(id: Long, archive: Boolean): Result<VoiceNote>

    /**
     * 重新转写语音笔记
     * @param id 笔记ID
     * @return 更新后的语音笔记
     */
    suspend fun retranscribe(id: Long): Result<VoiceNote>

    /**
     * 获取收藏的语音笔记
     * @param page 页码
     * @param size 每页数量
     * @return 收藏的语音笔记列表
     */
    suspend fun getFavoriteVoiceNotes(page: Int = 0, size: Int = 20): Result<List<VoiceNote>>

    /**
     * 获取语音笔记统计
     * @return 统计信息
     */
    suspend fun getStatistics(): Result<VoiceNoteStatistics>

    /**
     * 获取本地语音笔记流
     * @return 本地语音笔记流
     */
    fun getLocalVoiceNotesFlow(): Flow<List<VoiceNote>>

    /**
     * 同步语音笔记
     */
    suspend fun syncVoiceNotes(): Result<Unit>
}
