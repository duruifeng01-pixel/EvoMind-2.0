package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 讨论洞察响应
 * 结束讨论后生成的AI洞察总结
 */
@Data
@Schema(description = "讨论洞察响应")
public class DiscussionInsightResponse {

    @Schema(description = "洞察ID")
    private Long id;

    @Schema(description = "讨论ID")
    private Long discussionId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "AI生成的核心观点总结")
    private String aiSummary;

    @Schema(description = "AI生成的关键洞察")
    private List<String> keyInsights;

    @Schema(description = "个人感悟/总结")
    private String personalInsight;

    @Schema(description = "讨论参与数据统计")
    private ParticipationStats stats;

    @Schema(description = "生成时间")
    private LocalDateTime createdAt;

    @Data
    @Schema(description = "参与统计数据")
    public static class ParticipationStats {
        @Schema(description = "发表的一级评论数")
        private Integer topLevelComments;

        @Schema(description = "发表的回复数")
        private Integer replies;

        @Schema(description = "获得的总点赞数")
        private Integer totalLikesReceived;

        @Schema(description = "讨论总参与人数")
        private Integer totalParticipants;
    }
}
