package com.evomind.data.repository

import com.evomind.data.model.ModerationRequest
import com.evomind.data.model.ModerationResponse
import com.evomind.data.model.QuickCheckRequest
import com.evomind.data.model.QuickCheckResponse
import com.evomind.data.model.ModerationStatistics
import com.evomind.data.model.ModerationLogItem
import com.evomind.data.remote.api.ModerationApi
import com.evomind.data.remote.dto.ApiResponse
import com.evomind.data.remote.dto.PagedResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 内容审核仓库
 */
@Singleton
class ModerationRepository @Inject constructor(
    private val moderationApi: ModerationApi
) {

    /**
     * 内容审核
     */
    suspend fun moderateContent(request: ModerationRequest): Result<ModerationResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = moderationApi.moderateContent(request)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "审核失败"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 快速敏感词检测
     */
    suspend fun quickCheck(content: String): Result<QuickCheckResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = moderationApi.quickCheck(QuickCheckRequest(content))
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "检测失败"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * AI生成内容审核
     */
    suspend fun moderateAiContent(
        content: String,
        contentType: String,
        aiModel: String? = null
    ): Result<ModerationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = mutableMapOf(
                "content" to content,
                "contentType" to contentType
            )
            aiModel?.let { request["aiModel"] = it }

            val response = moderationApi.moderateAiContent(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "审核失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取审核历史
     */
    suspend fun getModerationHistory(
        page: Int = 0,
        size: Int = 20
    ): Result<PagedResponse<ModerationLogItem>> = withContext(Dispatchers.IO) {
        try {
            val response = moderationApi.getModerationHistory(page, size)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "获取历史失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取审核统计
     */
    suspend fun getModerationStatistics(): Result<ModerationStatistics> =
        withContext(Dispatchers.IO) {
            try {
                val response = moderationApi.getModerationStatistics()
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.message ?: "获取统计失败"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}