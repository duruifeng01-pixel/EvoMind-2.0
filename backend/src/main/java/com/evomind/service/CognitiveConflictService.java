package com.evomind.service;

import com.evomind.entity.Card;
import com.evomind.entity.CognitiveConflict;

import java.util.List;

/**
 * 认知冲突服务
 * 检测新卡片与用户认知画像的冲突
 */
public interface CognitiveConflictService {

    /**
     * 检测卡片与用户认知体系的冲突
     * @param userId 用户ID
     * @param card 新卡片
     * @return 检测到的冲突列表
     */
    List<CognitiveConflict> detectConflicts(Long userId, Card card);

    /**
     * 获取用户的所有未确认冲突
     * @param userId 用户ID
     * @return 冲突列表
     */
    List<CognitiveConflict> getUnresolvedConflicts(Long userId);

    /**
     * 获取卡片的冲突信息
     * @param cardId 卡片ID
     * @param userId 用户ID
     * @return 冲突列表
     */
    List<CognitiveConflict> getConflictsByCard(Long cardId, Long userId);

    /**
     * 确认冲突
     * @param conflictId 冲突ID
     * @param userId 用户ID
     */
    void acknowledgeConflict(Long conflictId, Long userId);

    /**
     * 忽略冲突
     * @param conflictId 冲突ID
     * @param userId 用户ID
     */
    void dismissConflict(Long conflictId, Long userId);

    /**
     * 获取未确认冲突数量
     * @param userId 用户ID
     * @return 冲突数量
     */
    long getUnresolvedConflictCount(Long userId);

    /**
     * 在卡片保存时自动检测冲突
     * @param userId 用户ID
     * @param cardId 卡片ID
     */
    void autoDetectOnCardSave(Long userId, Long cardId);
}
