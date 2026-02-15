package com.evomind.dto.request;

import com.evomind.entity.UserCorpus;
import lombok.Data;

/**
 * 创建语料请求DTO
 */
@Data
public class CreateCorpusRequest {

    private String title;

    private String contentText;

    private String summaryText;

    private String oneSentenceSummary;

    private String corpusType;

    private String sourceType;

    private Long sourceId;

    private String sourceRef;

    private Long discussionId;

    private String keywords;

    private Integer readingTimeMinutes;

    private Long relatedCardId;

    public UserCorpus.CorpusType getCorpusTypeEnum() {
        if (corpusType == null) {
            return UserCorpus.CorpusType.USER_NOTE;
        }
        try {
            return UserCorpus.CorpusType.valueOf(corpusType);
        } catch (IllegalArgumentException e) {
            return UserCorpus.CorpusType.USER_NOTE;
        }
    }

    public UserCorpus.SourceType getSourceTypeEnum() {
        if (sourceType == null) {
            return null;
        }
        try {
            return UserCorpus.SourceType.valueOf(sourceType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
