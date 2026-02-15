package com.evomind.data.repository

import com.evomind.data.remote.api.ChallengeApi
import com.evomind.data.remote.dto.response.ChallengeTaskResponseDto
import com.evomind.data.remote.dto.response.SubmitArtifactRequestDto
import com.evomind.domain.model.ChallengeTask
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

interface ChallengeRepository {
    suspend fun getCurrentChallenge(): Result<ChallengeTask>
    suspend fun getChallenge(id: Long): Result<ChallengeTask>
    suspend fun updateChallengeStatus(id: Long, completed: Boolean): Result<ChallengeTask>
    suspend fun submitArtifact(id: Long, title: String, content: String, artifactType: String): Result<ChallengeTask>
    suspend fun claimReward(id: Long): Result<ChallengeTask>
    suspend fun recordActivity(activityType: String): Result<Unit>
}

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val api: ChallengeApi
) : ChallengeRepository {

    override suspend fun getCurrentChallenge(): Result<ChallengeTask> = try {
        val response = api.getCurrentChallenge()
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取挑战任务失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getChallenge(id: Long): Result<ChallengeTask> = try {
        val response = api.getChallenge(id)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "获取任务详情失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateChallengeStatus(id: Long, completed: Boolean): Result<ChallengeTask> = try {
        val response = api.updateChallengeStatus(id, completed)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "更新状态失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun submitArtifact(
        id: Long,
        title: String,
        content: String,
        artifactType: String
    ): Result<ChallengeTask> = try {
        val request = SubmitArtifactRequestDto(title, content, artifactType)
        val response = api.submitArtifact(id, request)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "提交作品失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun claimReward(id: Long): Result<ChallengeTask> = try {
        val response = api.claimReward(id)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(response.body()!!.data!!.toDomain())
        } else {
            Result.failure(Exception(response.body()?.message ?: "领取奖励失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun recordActivity(activityType: String): Result<Unit> = try {
        val response = api.recordActivity(activityType)
        if (response.isSuccessful && response.body()?.code == 200) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.body()?.message ?: "记录活动失败"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun ChallengeTaskResponseDto.toDomain(): ChallengeTask {
        return ChallengeTask(
            id = id ?: 0,
            dateKey = dateKey ?: "",
            title = title ?: "",
            description = description ?: "",
            taskType = ChallengeTask.TaskType.fromString(taskType),
            targetCount = targetCount ?: 1,
            rewardPoints = rewardPoints ?: 10,
            rewardTrialDays = rewardTrialDays ?: 0,
            isToday = isToday ?: false,
            currentCount = currentCount ?: 0,
            progressPercent = progressPercent ?: 0,
            isCompleted = isCompleted ?: false,
            rewardClaimed = rewardClaimed ?: false,
            completedAt = completedAt
        )
    }
}
