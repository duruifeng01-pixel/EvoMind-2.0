package com.evomind.data.remote.dto.response

data class ChallengeTaskResponseDto(
    val id: Long?,
    val dateKey: String?,
    val title: String?,
    val description: String?,
    val taskType: String?,
    val targetCount: Int?,
    val rewardPoints: Int?,
    val rewardTrialDays: Int?,
    val isToday: Boolean?,
    val currentCount: Int?,
    val progressPercent: Int?,
    val isCompleted: Boolean?,
    val rewardClaimed: Boolean?,
    val completedAt: String?
)

data class SubmitArtifactRequestDto(
    val title: String?,
    val content: String?,
    val artifactType: String?
)
