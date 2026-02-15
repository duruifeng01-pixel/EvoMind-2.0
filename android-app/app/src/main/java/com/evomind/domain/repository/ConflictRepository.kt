package com.evomind.domain.repository

import com.evomind.domain.model.CardConflict
import kotlinx.coroutines.flow.Flow

/**
 * 冲突检测仓库接口
 */
interface ConflictRepository {

    /**
     * 检测卡片冲突
     */
    suspend fun detectConflicts(cardId: Long): Result<List<CardConflict>>

    /**
     * 获取未确认冲突
     */
    suspend fun getUnresolvedConflicts(): Result<List<CardConflict>>

    /**
     * 获取卡片相关冲突
     */
    suspend fun getConflictsByCard(cardId: Long): Result<List<CardConflict>>

    /**
     * 确认冲突
     */
    suspend fun acknowledgeConflict(conflictId: Long): Result<Unit>

    /**
     * 检查卡片间冲突
     */
    suspend fun checkConflictBetween(cardId1: Long, cardId2: Long): Result<Boolean>

    /**
     * 获取未确认冲突数量
     */
    suspend fun getUnresolvedConflictCount(): Result<Long>

    /**
     * 监听未确认冲突数量变化（用于Badge显示）
     */
    fun observeUnresolvedConflictCount(): Flow<Long>
}
