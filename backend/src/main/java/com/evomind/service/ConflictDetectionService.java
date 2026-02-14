package com.evomind.service;

import com.evomind.entity.Card;
import com.evomind.entity.CardConflict;

import java.util.List;

public interface ConflictDetectionService {

    /**
     * 检测卡片与其他卡片的冲突
     * @param cardId 卡片ID
     * @return 检测到的冲突列表
     */
    List<CardConflict> detectConflicts(Long cardId);

    /**
     * 获取用户的所有未确认冲突
     * @param userId 用户ID
     * @return 冲突列表
     */
    List<CardConflict> getUnresolvedConflicts(Long userId);

    /**
     * 获取卡片相关的所有冲突
     * @param cardId 卡片ID
     * @param userId 用户ID
     * @return 冲突列表
     */
    List<CardConflict> getConflictsByCard(Long cardId, Long userId);

    /**
     * 确认冲突（标记为已查看）
     * @param conflictId 冲突ID
     * @param userId 用户ID
     */
    void acknowledgeConflict(Long conflictId, Long userId);

    /**
     * 检查两张卡片是否存在冲突
     * @param cardId1 卡片1 ID
     * @param cardId2 卡片2 ID
     * @param userId 用户ID
     * @return 是否存在冲突
     */
    boolean hasConflictBetween(Long cardId1, Long cardId2, Long userId);

    /**
     * 获取用户未确认的冲突数量
     * @param userId 用户ID
     * @return 冲突数量
     */
    long getUnresolvedConflictCount(Long userId);
}
