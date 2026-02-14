package com.evomind.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DrilldownResponse {

    private Long cardId;
    private String nodeId;
    private String nodeText;
    private String originalParagraph;
    private Integer paragraphIndex;
    private Long sourceContentId;
    private String sourceTitle;
    private String sourceAuthor;
    private String warningMessage;
}
