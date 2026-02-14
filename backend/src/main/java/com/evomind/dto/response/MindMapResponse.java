package com.evomind.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MindMapResponse {

    private Long cardId;
    private String cardTitle;
    private List<MindMapNodeResponse> nodes;
    private Integer totalNodes;

    @Data
    @Builder
    public static class MindMapNodeResponse {
        private String nodeId;
        private String parentNodeId;
        private String text;
        private String description;
        private String nodeType;
        private Integer level;
        private Integer sortOrder;
        private Boolean hasOriginalReference;
        private Long originalContentId;
        private Integer originalParagraphIndex;
        private Boolean isExpanded;
        private List<MindMapNodeResponse> children;
    }
}
