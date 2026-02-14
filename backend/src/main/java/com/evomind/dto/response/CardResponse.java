package com.evomind.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认知卡片响应DTO
 */
@Data
@Builder
public class CardResponse {
    
    private Long id;
    private String title;
    private String summaryText;
    private String oneSentenceSummary;
    private Long sourceId;
    private String sourceUrl;
    private String sourceTitle;
    private Boolean isFavorite;
    private Boolean isArchived;
    private Boolean hasConflict;
    private Integer viewCount;
    private LocalDateTime lastViewedAt;
    private String generateStatus;
    private String keywords;
    private Integer readingTimeMinutes;
    private Integer tokenUsed;
    private String aiModel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // AI生成的扩展信息
    private List<AiGeneratedContentResponse.GoldenQuote> goldenQuotes;
    private List<AiGeneratedContentResponse.ExtractedCase> cases;
}
