package com.evomind.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * AI生成内容响应
 */
@Data
@Builder
public class AiGeneratedContentResponse {
    
    /**
     * 卡片标题
     */
    private String title;
    
    /**
     * 一句话导读
     */
    private String oneSentenceSummary;
    
    /**
     * 核心观点摘要
     */
    private String summaryText;
    
    /**
     * 关键词标签
     */
    private List<String> keywords;
    
    /**
     * 预计阅读时长（分钟）
     */
    private Integer readingTimeMinutes;
    
    /**
     * 核心金句列表
     */
    private List<GoldenQuote> goldenQuotes;
    
    /**
     * 案例提取列表
     */
    private List<ExtractedCase> cases;
    
    /**
     * 脑图JSON结构
     */
    private String mindMapJson;
    
    /**
     * 使用的Token数
     */
    private Integer tokenUsed;
    
    /**
     * 生成耗时（毫秒）
     */
    private Long generationTimeMs;
    
    /**
     * 金句数据结构
     */
    @Data
    @Builder
    public static class GoldenQuote {
        /**
         * 金句内容
         */
        private String content;
        
        /**
         * 金句解释/点评
         */
        private String explanation;
        
         /**
         * 在原文中的位置
         */
        private Integer paragraphIndex;
    }
    
    /**
     * 案例数据结构
     */
    @Data
    @Builder
    public static class ExtractedCase {
        /**
         * 案例标题
         */
        private String title;
        
        /**
         * 案例内容
         */
        private String content;
        
        /**
         * 案例类型：personal, business, historical, scientific
         */
        private String caseType;
        
        /**
         * 关联的核心观点
         */
        private String relatedPoint;
    }
}
