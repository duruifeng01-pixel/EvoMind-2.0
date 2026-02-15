package com.evomind.domain.usecase

import com.evomind.data.repository.SocraticDialogueRepository
import com.evomind.domain.model.*
import javax.inject.Inject

/**
 * 开始苏格拉底式对话
 */
class StartSocraticDialogueUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(
        discussionId: Long,
        initialThought: String? = null,
        maxRounds: Int = 5
    ): Result<SocraticDialogue> {
        return repository.startDialogue(discussionId, initialThought, maxRounds)
    }
}

/**
 * 发送对话消息
 */
class SendSocraticMessageUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(dialogueId: Long, content: String): Result<SocraticMessage> {
        return repository.sendMessage(dialogueId, content)
    }
}

/**
 * 获取对话详情
 */
class GetSocraticDialogueUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(dialogueId: Long): Result<SocraticDialogue> {
        return repository.getDialogue(dialogueId)
    }
}

/**
 * 获取对话消息列表
 */
class GetSocraticMessagesUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(dialogueId: Long): Result<List<SocraticMessage>> {
        return repository.getDialogueMessages(dialogueId)
    }
}

/**
 * 获取用户对话列表
 */
class GetUserSocraticDialoguesUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(page: Int = 0, size: Int = 10): Result<Pair<List<SocraticDialogue>, Boolean>> {
        return repository.getUserDialogues(page, size)
    }
}

/**
 * 结束对话并生成洞察
 */
class FinalizeSocraticDialogueUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(dialogueId: Long, satisfaction: Int? = null): Result<SocraticInsight> {
        return repository.finalizeDialogue(dialogueId, satisfaction)
    }
}

/**
 * 放弃对话
 */
class AbandonSocraticDialogueUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(dialogueId: Long): Result<Unit> {
        return repository.abandonDialogue(dialogueId)
    }
}

/**
 * 获取活动对话
 */
class GetActiveSocraticDialogueUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(discussionId: Long): Result<SocraticDialogue?> {
        return repository.getActiveDialogue(discussionId)
    }
}

/**
 * 检查是否可以开始对话
 */
class CanStartSocraticDialogueUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(discussionId: Long): Result<Boolean> {
        return repository.canStartDialogue(discussionId)
    }
}

/**
 * 重新生成AI回复
 */
class RegenerateSocraticResponseUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(messageId: Long): Result<SocraticMessage> {
        return repository.regenerateResponse(messageId)
    }
}

/**
 * 获取对话统计
 */
class GetSocraticStatsUseCase @Inject constructor(
    private val repository: SocraticDialogueRepository
) {
    suspend operator fun invoke(): Result<DialogueStats> {
        return repository.getDialogueStats()
    }
}
