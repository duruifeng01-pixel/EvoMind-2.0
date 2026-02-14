package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 讨论主题响应DTO
 */
@Data
@Schema(description = "讨论主题响应")
public class DiscussionResponse {

    @Schema(description = "讨论ID")
    private Long id;

    @Schema(description = "日期Key：20250214")
    private String dateKey;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "话题标签")
    private String topicTag;

    @Schema(description = "关联的认知卡片列表")
    private List<RelatedCard> relatedCards;

    @Schema(description = "关联的信息源列表")
    private List<RelatedSource> relatedSources;

    @Schema(description = "参与人数")
    private Integer participantCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(description = "是否已参与")
    private Boolean hasParticipated;

    @Data
    @Schema(description = "关联的认知卡片")
    public static class RelatedCard {
        @Schema(description = "卡片ID")
        private Long cardId;

        @Schema(description = "卡片标题")
        private String title;

        @Schema(description = "来源名称")
        private String sourceName;
    }

    @Data
    @Schema(description = "关联的信息源")
    public static class RelatedSource {
        @Schema(description = "信息源ID")
        private Long sourceId;

        @Schema(description = "信息源名称")
        private String name;

        @Schema(description = "平台类型")
        private String platform;
    }
}
