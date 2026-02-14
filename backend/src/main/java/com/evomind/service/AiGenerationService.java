package com.evomind.service;

import com.evomind.dto.response.AiGeneratedContentResponse;

/**
 * AI内容生成服务
 */
public interface AiGenerationService {
    
    /**
     * 根据原文生成认知卡片内容
     *
     * @param originalContent 原文内容
     * @param title 内容标题（可选）
     * @param generateMindMap 是否生成脑图
     * @return AI生成的卡片内容
     */
    AiGeneratedContentResponse generateCardContent(String originalContent, String title, boolean generateMindMap);
    
    /**
     * 生成脑图结构
     *
     * @param summaryText 摘要文本
     * @param keyPoints 核心观点
     * @return 脑图JSON字符串
     */
    String generateMindMap(String summaryText, String keyPoints);
    
    /**
     * 提取金句
     *
     * @param content 原文内容
     * @return 金句列表JSON
     */
    String extractGoldenQuotes(String content);
    
    /**
     * 提取案例
     *
     * @param content 原文内容
     * @return 案例列表JSON
     */
    String extractCases(String content);
    
    /**
     * 生成一句话导读
     *
     * @param content 原文内容
     * @return 一句话导读
     */
    String generateOneSentenceSummary(String content);
    
    /**
     * 提取关键词
     *
     * @param content 原文内容
     * @return 关键词列表，逗号分隔
     */
    String extractKeywords(String content);
}
