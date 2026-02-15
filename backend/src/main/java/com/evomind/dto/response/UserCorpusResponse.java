package com.evomind.dto.response;

import com.evomind.entity.UserCorpus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户语料库响应 DTO
 */
@Data
public class UserCorpusResponse {

    private Long id;
    private Long userId;
    private String title;
    private String summaryText;
    private String oneSentenceSummary;
    private String corpusType;
    private String corpusTypeDisplay;
    private String sourceType;
    private Long sourceId;
    private String sourceRef;
    private Long discussionId;
    private String keywords;
    private Integer readingTimeMinutes;
    private Boolean isFavorite;
    private Boolean isPinned;
    private LocalDateTime pinnedAt;
    private Boolean isArchived;
    private LocalDateTime archivedAt;
    private Integer viewCount;
    private LocalDateTime lastViewedAt;
    private Long relatedCardId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为响应 DTO
     */
    public static UserCorpusResponse fromEntity(UserCorpus corpus) {
        if (corpus == null) {
            return null;
        }
        UserCorpusResponse response = new UserCorpusResponse();
        response.setId(corpus.getId());
        response.setUserId(corpus.getUserId());
        response.setTitle(corpus.getTitle());
        response.setSummaryText(corpus.getSummaryText());
        response.setOneSentenceSummary(corpus.getOneSentenceSummary());
        response.setCorpusType(corpus.getCorpusType() != null ? corpus.getCorpusType().name() : null);
        response.setCorpusTypeDisplay(corpus.getTypeDisplayName());
        response.setSourceType(corpus.getSourceType() != null ? corpus.getSourceType().name() : null);
        response.setSourceId(corpus.getSourceId());
        response.setSourceRef(corpus.getSourceRef());
        response.setDiscussionId(corpus.getDiscussionId());
        response.setKeywords(corpus.getKeywords());
        response.setReadingTimeMinutes(corpus.getReadingTimeMinutes());
        response.setIsFavorite(corpus.getIsFavorite());
        response.setIsPinned(corpus.getIsPinned());
        response.setPinnedAt(corpus.getPinnedAt());
        response.setIsArchived(corpus.getIsArchived());
        response.setArchivedAt(corpus.getArchivedAt());
        response.setViewCount(corpus.getViewCount());
        response.setLastViewedAt(corpus.getLastViewedAt());
        response.setRelatedCardId(corpus.getRelatedCardId());
        response.setCreatedAt(corpus.getCreatedAt());
        response.setUpdatedAt(corpus.getUpdatedAt());
        return response;
    }
}
