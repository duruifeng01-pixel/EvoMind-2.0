package com.evomind.service;

import com.evomind.entity.Card;
import com.evomind.entity.CrawledContent;

import java.util.List;

/**
 * 内容处理服务接口
 * 负责将采集的内容转换为认知卡片
 */
public interface ContentProcessingService {

    /**
     * 处理单条采集内容，生成认知卡片
     */
    Card processToCard(Long crawledContentId);

    /**
     * 批量处理采集内容
     */
    List<Card> batchProcessToCards(List<Long> crawledContentIds);

    /**
     * 提取核心观点
     */
    String extractCoreOpinion(String content);

    /**
     * 生成脑图结构
     */
    String generateMindMapStructure(String title, String content);

    /**
     * 评估内容重要性
     */
    int evaluateImportance(String content, String category);

    /**
     * 自动分类内容
     */
    String autoClassify(String title, String content);

    /**
     * 提取关键词
     */
    List<String> extractKeywords(String title, String content);

    /**
     * 获取待处理的内容列表
     */
    List<CrawledContent> getPendingContents(Long userId, int limit);

    /**
     * 处理结果
     */
    class ProcessingResult {
        private boolean success;
        private Card card;
        private String errorMessage;

        public ProcessingResult(boolean success, Card card, String errorMessage) {
            this.success = success;
            this.card = card;
            this.errorMessage = errorMessage;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public Card getCard() { return card; }
        public String getErrorMessage() { return errorMessage; }
    }
}
