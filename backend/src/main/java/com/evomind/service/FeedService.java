package com.evomind.service;

import com.evomind.dto.response.CardResponse;

import java.util.List;

/**
 * Feed流服务接口
 * 实现7:3智能混合信息流
 */
public interface FeedService {

    /**
     * 获取用户Feed流（7:3混合）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 混合后的卡片列表
     */
    List<CardResponse> getMixedFeed(Long userId, int page, int size);

    /**
     * 获取自选源内容（70%）
     * @param userId 用户ID
     * @param limit 数量
     * @return 自选源卡片列表
     */
    List<CardResponse> getUserSourceContent(Long userId, int limit);

    /**
     * 获取系统推荐内容（30%）
     * @param userId 用户ID
     * @param limit 数量
     * @return 系统推荐卡片列表
     */
    List<CardResponse> getSystemRecommendations(Long userId, int limit);

    /**
     * 记录阅读行为
     * @param userId 用户ID
     * @param cardId 卡片ID
     * @param durationSeconds 阅读时长（秒）
     * @param readPercentage 阅读完成度（0-100）
     */
    void trackReadingBehavior(Long userId, Long cardId, int durationSeconds, int readPercentage);

    /**
     * 记录互动行为
     * @param userId 用户ID
     * @param cardId 卡片ID
     * @param interactionType 互动类型
     */
    void trackInteraction(Long userId, Long cardId, String interactionType);

    /**
     * 刷新Feed（重新混合）
     * @param userId 用户ID
     */
    void refreshFeed(Long userId);

    /**
     * 获取Feed流统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    FeedStats getFeedStats(Long userId);

    /**
     * Feed流统计
     */
    class FeedStats {
        private long totalCards;
        private long userSourceCards;
        private long recommendedCards;
        private double diversityScore;
        private double echoChamberRisk;

        // Getters and Setters
        public long getTotalCards() { return totalCards; }
        public void setTotalCards(long totalCards) { this.totalCards = totalCards; }
        public long getUserSourceCards() { return userSourceCards; }
        public void setUserSourceCards(long userSourceCards) { this.userSourceCards = userSourceCards; }
        public long getRecommendedCards() { return recommendedCards; }
        public void setRecommendedCards(long recommendedCards) { this.recommendedCards = recommendedCards; }
        public double getDiversityScore() { return diversityScore; }
        public void setDiversityScore(double diversityScore) { this.diversityScore = diversityScore; }
        public double getEchoChamberRisk() { return echoChamberRisk; }
        public void setEchoChamberRisk(double echoChamberRisk) { this.echoChamberRisk = echoChamberRisk; }
    }
}
