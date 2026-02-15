package com.evomind.data.repository

import com.evomind.data.remote.api.ConflictApi
import com.evomind.data.remote.dto.ConflictResponseDto
import com.evomind.domain.model.CardConflict
import com.evomind.domain.model.ConflictType
import com.evomind.domain.repository.ConflictRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 冲突检测仓库实现
 */
@Singleton
class ConflictRepositoryImpl @Inject constructor(
    private val conflictApi: ConflictApi
) : ConflictRepository {

    private val _unresolvedCount = MutableStateFlow(0L)
    override fun observeUnresolvedConflictCount(): Flow<Long> = _unresolvedCount.asStateFlow()

    override suspend fun detectConflicts(cardId: Long): Result<List<CardConflict>> {
        return try {
            val response = conflictApi.detectConflicts(cardId)
            if (response.isSuccess()) {
                val conflicts = response.data?.map { it.toDomain() } ?: emptyList()
                updateUnresolvedCount()
                Result.success(conflicts)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUnresolvedConflicts(): Result<List<CardConflict>> {
        return try {
            val response = conflictApi.getUnresolvedConflicts()
            if (response.isSuccess()) {
                val conflicts = response.data?.map { it.toDomain() } ?: emptyList()
                _unresolvedCount.value = conflicts.size.toLong()
                Result.success(conflicts)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConflictsByCard(cardId: Long): Result<List<CardConflict>> {
        return try {
            val response = conflictApi.getConflictsByCard(cardId)
            if (response.isSuccess()) {
                val conflicts = response.data?.map { it.toDomain() } ?: emptyList()
                Result.success(conflicts)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acknowledgeConflict(conflictId: Long): Result<Unit> {
        return try {
            val response = conflictApi.acknowledgeConflict(conflictId)
            if (response.isSuccess()) {
                updateUnresolvedCount()
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkConflictBetween(cardId1: Long, cardId2: Long): Result<Boolean> {
        return try {
            val response = conflictApi.checkConflictBetween(cardId1, cardId2)
            if (response.isSuccess()) {
                Result.success(response.data ?: false)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUnresolvedConflictCount(): Result<Long> {
        return try {
            val response = conflictApi.getUnresolvedConflictCount()
            if (response.isSuccess()) {
                val count = response.data ?: 0L
                _unresolvedCount.value = count
                Result.success(count)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateUnresolvedCount() {
        getUnresolvedConflictCount()
    }

    private fun ConflictResponseDto.toDomain(): CardConflict {
        return CardConflict(
            id = id,
            cardId1 = cardId1,
            cardId2 = cardId2,
            cardTitle1 = cardTitle1 ?: "",
            cardTitle2 = cardTitle2 ?: "",
            cardViewpoint1 = cardViewpoint1 ?: "",
            cardViewpoint2 = cardViewpoint2 ?: "",
            conflictType = ConflictType.fromValue(conflictType),
            conflictDescription = conflictDescription ?: "",
            topic = topic ?: "",
            similarityScore = similarityScore ?: BigDecimal.ZERO,
            conflictScore = conflictScore ?: BigDecimal.ZERO,
            isAcknowledged = isAcknowledged ?: false,
            aiAnalysis = aiAnalysis ?: "",
            createdAt = createdAt ?: java.time.LocalDateTime.now(),
            acknowledgedAt = acknowledgedAt
        )
    }
}
