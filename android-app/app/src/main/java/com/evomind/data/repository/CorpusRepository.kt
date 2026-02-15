package com.evomind.data.repository

import com.evomind.data.remote.api.CorpusApi
import com.evomind.data.remote.dto.CreateCorpusRequestDto
import com.evomind.data.remote.dto.response.*
import com.evomind.domain.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户语料库Repository接口
 */
interface CorpusRepository {
    suspend fun createCorpus(
        title: String,
        contentText: String?,
        summaryText: String?,
        corpusType: String,
        sourceType: String?,
        sourceId: Long?,
        discussionId: Long?,
        keywords: String?
    ): Result<UserCorpus>
    suspend fun getUserCorpus(page: Int, size: Int): Result<PagedResult<UserCorpus>>
    suspend fun getCorpusDetail(id: Long): Result<UserCorpus>
    suspend fun getArchivedCorpus(page: Int, size: Int): Result<PagedResult<UserCorpus>>
    suspend fun getFavoriteCorpus(page: Int, size: Int): Result<PagedResult<UserCorpus>>
    suspend fun getCorpusByType(type: String, page: Int, size: Int): Result<PagedResult<UserCorpus>>
    suspend fun getPinnedCorpus(): Result<List<UserCorpus>>
    suspend fun searchCorpus(keyword: String, page: Int, size: Int): Result<PagedResult<UserCorpus>>
    suspend fun toggleFavorite(id: Long): Result<UserCorpus>
    suspend fun togglePin(id: Long): Result<UserCorpus>
    suspend fun archiveCorpus(id: Long): Result<UserCorpus>
    suspend fun unarchiveCorpus(id: Long): Result<UserCorpus>
    suspend fun deleteCorpus(id: Long): Result<Unit>
    suspend fun getCorpusStats(): Result<CorpusStats>
    suspend fun getInsightsByDiscussion(discussionId: Long): Result<List<UserCorpus>>
}

/**
 * 用户语料库Repository实现
 */
@Singleton
class CorpusRepositoryImpl @Inject constructor(
    private val api: CorpusApi
) : CorpusRepository {

    override suspend fun createCorpus(
        title: String,
        contentText: String?,
        summaryText: String?,
        corpusType: String,
        sourceType: String?,
        sourceId: Long?,
        discussionId: Long?,
        keywords: String?
    ): Result<UserCorpus> = try {
        val request = CreateCorpusRequestDto(
            title = title,
            contentText = contentText,
            summaryText = summaryText,
            oneSentenceSummary = null,
            corpusType = corpusType,
            sourceType = sourceType,
            sourceId = sourceId,
            sourceRef = null,
            discussionId = discussionId,
            keywords = keywords,
            readingTimeMinutes = null,
            relatedCardId = null
        )
        val response = api.createCorpus(request)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "创建语料失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserCorpus(page: Int, size: Int): Result<PagedResult<UserCorpus>> = try {
        val response = api.getUserCorpus(page, size)
        if (response.isSuccessful && response.body()?.code == 200) {
            val data = response.body()!!.data!!
            Result.success(
                PagedResult(
                    items = data.content.map { it.toDomain() },
                    page = data.page,
                    pageSize = data.size,
                    totalElements = data.totalElements,
                    totalPages = data.totalPages,
                    hasMore = !data.last
                )
            )
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取语料库列表失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCorpusDetail(id: Long): Result<UserCorpus> = try {
        val response = api.getCorpusDetail(id)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomainDetail())
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取语料详情失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getArchivedCorpus(page: Int, size: Int): Result<PagedResult<UserCorpus>> = try {
        val response = api.getArchivedCorpus(page, size)
        if (response.isSuccessful && response.body()?.code == 200) {
            val data = response.body()!!.data!!
            Result.success(
                PagedResult(
                    items = data.content.map { it.toDomain() },
                    page = data.page,
                    pageSize = data.size,
                    totalElements = data.totalElements,
                    totalPages = data.totalPages,
                    hasMore = !data.last
                )
            )
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取归档语料失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getFavoriteCorpus(page: Int, size: Int): Result<PagedResult<UserCorpus>> = try {
        val response = api.getFavoriteCorpus(page, size)
        if (response.isSuccessful && response.body()?.code == 200) {
            val data = response.body()!!.data!!
            Result.success(
                PagedResult(
                    items = data.content.map { it.toDomain() },
                    page = data.page,
                    pageSize = data.size,
                    totalElements = data.totalElements,
                    totalPages = data.totalPages,
                    hasMore = !data.last
                )
            )
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取收藏语料失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCorpusByType(type: String, page: Int, size: Int): Result<PagedResult<UserCorpus>> = try {
        val response = api.getCorpusByType(type, page, size)
        if (response.isSuccessful && response.body()?.code == 200) {
            val data = response.body()!!.data!!
            Result.success(
                PagedResult(
                    items = data.content.map { it.toDomain() },
                    page = data.page,
                    pageSize = data.size,
                    totalElements = data.totalElements,
                    totalPages = data.totalPages,
                    hasMore = !data.last
                )
            )
        } else {
            Result.failure(Exception(response.body()?.message ?: "按类型获取语料失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getPinnedCorpus(): Result<List<UserCorpus>> = try {
        val response = api.getPinnedCorpus()
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.map { it.toDomain() })
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取置顶语料失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun searchCorpus(keyword: String, page: Int, size: Int): Result<PagedResult<UserCorpus>> = try {
        val response = api.searchCorpus(keyword, page, size)
        if (response.isSuccessful && response.body()?.code == 200) {
            val data = response.body()!!.data!!
            Result.success(
                PagedResult(
                    items = data.content.map { it.toDomain() },
                    page = data.page,
                    pageSize = data.size,
                    totalElements = data.totalElements,
                    totalPages = data.totalPages,
                    hasMore = !data.last
                )
            )
        } else {
            Result.failure(Exception(response.body()?.message ?: "搜索语料失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun toggleFavorite(id: Long): Result<UserCorpus> = try {
        val response = api.toggleFavorite(id)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "收藏操作失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun togglePin(id: Long): Result<UserCorpus> = try {
        val response = api.togglePin(id)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "置顶操作失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun archiveCorpus(id: Long): Result<UserCorpus> = try {
        val response = api.archiveCorpus(id)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "归档操作失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun unarchiveCorpus(id: Long): Result<UserCorpus> = try {
        val response = api.unarchiveCorpus(id)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "取消归档失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteCorpus(id: Long): Result<Unit> = try {
        val response = api.deleteCorpus(id)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.body()?.message ?: "删除语料失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCorpusStats(): Result<CorpusStats> = try {
        val response = api.getCorpusStats()
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取统计失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getInsightsByDiscussion(discussionId: Long): Result<List<UserCorpus>> = try {
        val response = api.getInsightsByDiscussion(discussionId)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.map { it.toDomain() })
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取洞察失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ==================== DTO转换 ====================

    private fun UserCorpusResponseDto.toDomain(): UserCorpus {
        return UserCorpus(
            id = id,
            userId = userId,
            title = title,
            contentText = null,
            summaryText = summaryText,
            oneSentenceSummary = oneSentenceSummary,
            corpusType = UserCorpus.CorpusType.fromString(corpusType),
            sourceType = UserCorpus.SourceType.fromString(sourceType),
            sourceId = sourceId,
            sourceRef = sourceRef,
            discussionId = discussionId,
            keywords = keywords,
            readingTimeMinutes = readingTimeMinutes,
            isFavorite = isFavorite,
            isPinned = isPinned,
            pinnedAt = pinnedAt?.let { parseDateTime(it) },
            isArchived = isArchived,
            archivedAt = archivedAt?.let { parseDateTime(it) },
            viewCount = viewCount,
            lastViewedAt = lastViewedAt?.let { parseDateTime(it) },
            relatedCardId = relatedCardId,
            createdAt = parseDateTime(createdAt),
            updatedAt = parseDateTime(updatedAt)
        )
    }

    private fun UserCorpusDetailResponseDto.toDomainDetail(): UserCorpus {
        return UserCorpus(
            id = id,
            userId = userId,
            title = title,
            contentText = contentText,
            summaryText = summaryText,
            oneSentenceSummary = oneSentenceSummary,
            corpusType = UserCorpus.CorpusType.fromString(corpusType),
            sourceType = UserCorpus.SourceType.fromString(sourceType),
            sourceId = sourceId,
            sourceRef = sourceRef,
            discussionId = discussionId,
            keywords = keywords,
            readingTimeMinutes = readingTimeMinutes,
            isFavorite = isFavorite,
            isPinned = isPinned,
            pinnedAt = pinnedAt?.let { parseDateTime(it) },
            isArchived = isArchived,
            archivedAt = archivedAt?.let { parseDateTime(it) },
            viewCount = viewCount,
            lastViewedAt = lastViewedAt?.let { parseDateTime(it) },
            relatedCardId = relatedCardId,
            createdAt = parseDateTime(createdAt),
            updatedAt = parseDateTime(updatedAt)
        )
    }

    private fun CorpusStatsResponseDto.toDomain(): CorpusStats {
        return CorpusStats(
            total = total,
            socraticInsightCount = socraticInsightCount,
            userNoteCount = userNoteCount,
            highlightCount = highlightCount,
            aiSummaryCount = aiSummaryCount
        )
    }

    private fun parseDateTime(dateTimeStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}
