package com.evomind.domain.usecase

import com.evomind.domain.model.CognitiveConflict
import com.evomind.domain.model.Result
import com.evomind.domain.model.UserCognitiveProfile
import com.evomind.domain.repository.CognitiveConflictRepository
import javax.inject.Inject

/**
 * 认知冲突相关 UseCase
 */
class GetUnresolvedConflictsUseCase @Inject constructor(
    private val repository: CognitiveConflictRepository
) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): Result<List<CognitiveConflict>> {
        return repository.getUnresolvedConflicts(page, size)
    }
}

class DetectCognitiveConflictUseCase @Inject constructor(
    private val repository: CognitiveConflictRepository
) {
    suspend operator fun invoke(cardId: Long): Result<CognitiveConflict> {
        return repository.detectConflict(cardId)
    }
}

class AcknowledgeCognitiveConflictUseCase @Inject constructor(
    private val repository: CognitiveConflictRepository
) {
    suspend operator fun invoke(conflictId: Long): Result<Unit> {
        return repository.acknowledgeConflict(conflictId)
    }
}

class DismissCognitiveConflictUseCase @Inject constructor(
    private val repository: CognitiveConflictRepository
) {
    suspend operator fun invoke(conflictId: Long): Result<Unit> {
        return repository.dismissConflict(conflictId)
    }
}

class GetCognitiveProfilesUseCase @Inject constructor(
    private val repository: CognitiveConflictRepository
) {
    suspend operator fun invoke(): Result<List<UserCognitiveProfile>> {
        return repository.getCognitiveProfiles()
    }
}

class GetUnresolvedConflictCountUseCase @Inject constructor(
    private val repository: CognitiveConflictRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return repository.getUnresolvedConflictCount()
    }
}
