package com.evomind.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
