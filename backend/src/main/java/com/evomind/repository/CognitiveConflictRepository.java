package com.evomind.repository;

import com.evomind.entity.CognitiveConflict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 认知冲突Repository
 */
@Repository
public interface CognitiveConflictRepository extends JpaRepository<CognitiveConflict, Long> {

    /**
     * 根据用户ID查找所有未确认的认知冲突
     */
    List<CognitiveConflict> findByUserIdAndIsAcknowledgedFalseAndIsDismissedFalse(Long userId);

    /**
     * 根据卡片ID查找冲突
     */
    List<CognitiveConflict> findByCardIdAndUserId(Long cardId, Long userId);

    /**
     * 检查是否已存在冲突记录
     */
    boolean existsByCardIdAndProfileIdAndUserId(Long cardId, Long profileId, Long userId);

    /**
     * 统计未确认冲突数量
     */
    long countByUserIdAndIsAcknowledgedFalseAndIsDismissedFalse(Long userId);

    /**
     * 根据用户ID和主题查找冲突
     */
    List<CognitiveConflict> findByUserIdAndTopic(Long userId, String topic);
}
