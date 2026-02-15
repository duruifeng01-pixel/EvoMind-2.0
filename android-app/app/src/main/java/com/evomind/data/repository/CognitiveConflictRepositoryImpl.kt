package com.evomind.data.repository

import com.evomind.data.remote.api.CognitiveConflictApi
import com.evomind.data.remote.dto.response.UserCognitiveProfileDto
import com.evomind.domain.model.BeliefType
import com.evomind.domain.model.CognitiveConflict
import com.evomind.domain.model.Result
import com.evomind.domain.model.UserCognitiveProfile
import com.evomind.domain.repository.CognitiveConflictRepository
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认知冲突 Repository 实现
 */
@Singleton
class CognitiveConflictRepositoryImpl @Inject constructor(
    private val cognitiveConflictApi: CognitiveConflictApi
) : CognitiveConflictRepository {

    override suspend fun detectConflict(cardId: Long): Result<CognitiveConflict> {
        return try {
            val response = cognitiveConflictApi.detectConflict(cardId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it.toDomain())
                } ?: Result.Error("响应为空")
            } else {
                Result.Error("检测冲突失败: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络请求失败")
        }
    }

    override suspend fun getUnresolvedConflicts(
        page: Int,
        size: Int
    ): Result<List<CognitiveConflict>> {
        return try {
            val response = cognitiveConflictApi.getUnresolvedConflicts(page, size)
            if (response.isSuccessful) {
                val conflicts = response.body()?.conflicts?.map { it.toDomain() } ?: emptyList()
                Result.Success(conflicts)
            } else {
                Result.Error("获取冲突列表失败: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络请求失败")
        }
    }

    override suspend fun acknowledgeConflict(conflictId: Long): Result<Unit> {
        return try {
            val response = cognitiveConflictApi.acknowledgeConflict(conflictId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("确认冲突失败: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络请求失败")
        }
    }

    override suspend fun dismissConflict(conflictId: Long): Result<Unit> {
        return try {
            val response = cognitiveConflictApi.dismissConflict(conflictId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("忽略冲突失败: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络请求失败")
        }
    }

    override suspend fun getCognitiveProfiles(): Result<List<UserCognitiveProfile>> {
        return try {
            val response = cognitiveConflictApi.getCognitiveProfiles()
            if (response.isSuccessful) {
                val profiles = response.body()?.map { it.toDomain() } ?: emptyList()
                Result.Success(profiles)
            } else {
                Result.Error("获取认知画像失败: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络请求失败")
        }
    }

    override suspend fun rebuildProfiles(): Result<Unit> {
        return try {
            val response = cognitiveConflictApi.rebuildProfiles()
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("重建画像失败: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "网络请求失败")
        }
    }

    override suspend fun getUnresolvedConflictCount(): Result<Int> {
        return when (val result = getUnresolvedConflicts(0, 1)) {
            is Result.Success -> Result.Success(result.data.size)
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }

    private fun UserCognitiveProfileDto.toDomain(): UserCognitiveProfile {
        return UserCognitiveProfile(
            id = id,
            topic = topic,
            coreBelief = coreBelief,
            beliefType = BeliefType.valueOf(beliefType),
            confidenceLevel = confidenceLevel,
            evidenceCount = evidenceCount
        )
    }
}
