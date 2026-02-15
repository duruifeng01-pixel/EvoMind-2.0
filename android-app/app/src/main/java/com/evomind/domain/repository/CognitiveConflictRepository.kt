package com.evomind.domain.repository

import com.evomind.domain.model.CognitiveConflict
import com.evomind.domain.model.Result
import com.evomind.domain.model.UserCognitiveProfile

/**
 * 认知冲突 Repository 接口
 */
interface CognitiveConflictRepository {

    /**
     * 检测卡片与认知画像的冲突
     */
    suspend fun detectConflict(cardId: Long): Result<CognitiveConflict>

    /**
     * 获取未解决的冲突列表
     */
    suspend fun getUnresolvedConflicts(
        page: Int = 0,
        size: Int = 20
    ): Result<List<CognitiveConflict>>

    /**
     * 确认冲突（已阅）
     */
    suspend fun acknowledgeConflict(conflictId: Long): Result<Unit>

    /**
     * 忽略冲突
     */
    suspend fun dismissConflict(conflictId: Long): Result<Unit>

    /**
     * 获取用户认知画像列表
     */
    suspend fun getCognitiveProfiles(): Result<List<UserCognitiveProfile>>

    /**
     * 重建认知画像
     */
    suspend fun rebuildProfiles(): Result<Unit>

    /**
     * 获取未解决冲突数量（用于 Badge）
     */
    suspend fun getUnresolvedConflictCount(): Result<Int>
}
