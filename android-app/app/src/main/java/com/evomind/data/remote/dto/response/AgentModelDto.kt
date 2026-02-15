package com.evomind.data.remote.dto.response

data class AgentModelResponseDto(
    val id: Long?,
    val name: String?,
    val description: String?,
    val modelType: String?,
    val modelPath: String?,
    val isActive: Boolean?,
    val trainingStatus: String?,
    val accuracy: Double?,
    val version: Int?,
    val createdAt: String?,
    val updatedAt: String?
)

data class TrainingProgressDto(
    val modelId: Long?,
    val status: String?,
    val progress: Int?,
    val currentEpoch: Int?,
    val totalEpochs: Int?,
    val loss: Double?,
    val accuracy: Double?,
    val estimatedTimeRemaining: String?,
    val startedAt: String?
)

data class InferenceResultDto(
    val modelId: Long?,
    val input: String?,
    val output: String?,
    val confidence: Double?,
    val inferenceTimeMs: Long?
)
