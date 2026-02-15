package com.evomind.service;

import com.evomind.entity.Card;
import com.evomind.entity.UserInterestProfile;

import java.util.List;

/**
 * 推荐服务接口
 * 负责内容推荐和用户画像构建
 */
public interface RecommendationService {

    /**
     * 为用户推荐内容（30%系统推荐部分）
     * @param userId 用户ID
     * @param limit 推荐数量
     * @param excludeCardIds 需要排除的卡片ID（已读或已在70%中）
     * @return 推荐的卡片列表
     */
    List<Card> recommendCards(Long userId, int limit, List<Long> excludeCardIds);

    /**
     * 构建用户兴趣画像
     * @param userId 用户ID
     */
    void buildUserInterestProfile(Long userId);

    /**
     * 更新用户兴趣权重（基于阅读行为）
     * @param userId 用户ID
     * @param cardId 卡片ID
     * @param keywords 关键词
     * @param score 互动得分
     */
    void updateInterestWeight(Long userId, Long cardId, String keywords, int score);

    /**
     * 获取用户兴趣标签
     * @param userId 用户ID
     * @param topN 前N个
     * @return 兴趣标签列表
     */
    List<UserInterestProfile> getUserTopInterests(Long userId, int topN);

    /**
     * 检查信息茧房风险
     * @param userId 用户ID
     * @return 风险等级（0-1，越高风险越大）
     */
    double calculateEchoChamberRisk(Long userId);

    /**
     * 获取多样性推荐（信息茧房避免）
     * @param userId 用户ID
     * @param limit 数量
     * @return 多样性内容列表
     */
    List<Card> getDiverseRecommendations(Long userId, int limit);
}
