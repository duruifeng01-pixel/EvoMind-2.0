package com.evomind.domain.model

data class ChallengeTask(
    val id: Long = 0,
    val dateKey: String = "",
    val title: String = "",
    val description: String = "",
    val taskType: TaskType = TaskType.DAILY_CHECKIN,
    val targetCount: Int = 1,
    val rewardPoints: Int = 10,
    val rewardTrialDays: Int = 0,
    val isToday: Boolean = false,
    val currentCount: Int = 0,
    val progressPercent: Int = 0,
    val isCompleted: Boolean = false,
    val rewardClaimed: Boolean = false,
    val completedAt: String? = null
) {
    enum class TaskType(val displayName: String, val description: String) {
        READ_CARDS("阅读卡片", "阅读N张认知卡片"),
        ADD_SOURCES("添加信息源", "添加N个信息源"),
        CREATE_NOTES("创建笔记", "创建N条笔记"),
        COMPLETE_DISCUSSION("参与讨论", "参与今日讨论"),
        SHARE_INSIGHT("分享洞察", "分享一条洞见"),
        DAILY_CHECKIN("每日签到", "每日签到打卡");

        companion object {
            fun fromString(value: String?): TaskType {
                return entries.find { it.name == value } ?: DAILY_CHECKIN
            }
        }
    }

    fun getProgressText(): String = "$currentCount / $targetCount"
}

data class Artifact(
    val id: Long = 0,
    val taskId: Long = 0,
    val userId: Long = 0,
    val title: String = "",
    val content: String = "",
    val artifactType: ArtifactType = ArtifactType.TEXT,
    val createdAt: String? = null
) {
    enum class ArtifactType(val displayName: String) {
        TEXT("文字"),
        IMAGE("图片"),
        VOICE("语音"),
        VIDEO("视频");

        companion object {
            fun fromString(value: String?): ArtifactType {
                return entries.find { it.name == value } ?: TEXT
            }
        }
    }
}
