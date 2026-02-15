package com.evomind.data.remote.dto.response

data class AgentModelDto(
    val id: Long?,
    val name: String?,
    val description: String?,
    val modelType: String?,
    val status: String?,
    val trainedAt: String?,
    val corpusCount: Int?,
    val version: Int?
)

data class TrainingJobDto(
    val id: Long?,
    val agentId: Long?,
    val status: String?,
    val progress: Int?,
    val startedAt: String?,
    val completedAt: String?,
    val error: String?
)
