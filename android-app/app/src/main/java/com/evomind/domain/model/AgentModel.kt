package com.evomind.domain.model

data class AgentModel(
    val id: Long = 0,
    val name: String = "",
    val description: String? = null,
    val modelType: String = "TEXT_CLASSIFICATION",
    val modelPath: String? = null,
    val isActive: Boolean = false,
    val trainingStatus: TrainingStatus = TrainingStatus.IDLE,
    val accuracy: Double = 0.0,
    val version: Int = 1,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    enum class TrainingStatus(val displayName: String) {
        IDLE("未训练"),
        TRAINING("训练中"),
        COMPLETED("训练完成"),
        FAILED("训练失败");

        companion object {
            fun fromString(value: String?): TrainingStatus {
                return entries.find { it.name == value } ?: IDLE
            }
        }
    }
}

data class TrainingProgress(
    val modelId: Long = 0,
    val status: String = "",
    val progress: Int = 0,
    val currentEpoch: Int = 0,
    val totalEpochs: Int = 10,
    val loss: Double = 0.0,
    val accuracy: Double = 0.0,
    val estimatedTimeRemaining: String? = null,
    val startedAt: String? = null
)

data class InferenceResult(
    val modelId: Long = 0,
    val input: String = "",
    val output: String = "",
    val confidence: Double = 0.0,
    val inferenceTimeMs: Long = 0
)
