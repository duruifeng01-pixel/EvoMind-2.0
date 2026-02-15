package com.evomind.dto.response;

import com.evomind.entity.UserCorpus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户语料库详情响应 DTO
 * 包含完整内容文本
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserCorpusDetailResponse extends UserCorpusResponse {

    private String contentText;

    /**
     * 从实体转换为详情响应 DTO
     */
    public static UserCorpusDetailResponse fromEntity(UserCorpus corpus) {
        if (corpus == null) {
            return null;
        }
        UserCorpusDetailResponse response = new UserCorpusDetailResponse();
        // 复制基础字段
        response.setId(corpus.getId());
        response.setUserId(corpus.getUserId());
        response.setTitle(corpus.getTitle());
        response.setContentText(corpus.getContentText());
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
