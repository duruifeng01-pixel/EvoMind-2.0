package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 苏格拉底式对话洞察响应
 */
@Data
@Schema(description = "苏格拉底式对话洞察响应")
public class SocraticInsightResponse {

    @Schema(description = "洞察ID")
    private Long id;

    @Schema(description = "对话ID")
    private Long dialogueId;

    @Schema(description = "讨论ID")
    private Long discussionId;

    @Schema(description = "核心洞察")
    private String coreInsight;

    @Schema(description = "思考的演变过程")
    private List<ThinkingEvolution> thinkingEvolution;

    @Schema(description = "关键转折点")
    private List<KeyTurningPoint> turningPoints;

    @Schema(description = "未解问题")
    private List<String> unresolvedQuestions;

    @Schema(description = "深度反思建议")
    private String reflectionSuggestion;

    @Schema(description = "相关认知卡片")
    private List<RelatedCard> relatedCards;

    @Schema(description = "对话轮次统计")
    private RoundStats roundStats;

    @Schema(description = "生成时间")
    private LocalDateTime generatedAt;

    /**
     * 思考演变
     */
    @Data
    @Schema(description = "思考演变")
    public static class ThinkingEvolution {
        @Schema(description = "阶段")
        private Integer stage;

        @Schema(description = "阶段描述")
        private String description;

        @Schema(description = "用户的思考")
        private String userThinking;

        @Schema(description = "AI的引导")
        private String aiGuidance;
    }

    /**
     * 关键转折点
     */
    @Data
    @Schema(description = "关键转折点")
    public static class KeyTurningPoint {
        @Schema(description = "轮次")
        private Integer round;

        @Schema(description = "转折描述")
        private String description;

        @Schema(description = "前后对比")
        private String beforeAfter;
    }

    /**
     * 相关认知卡片
     */
    @Data
    @Schema(description = "相关认知卡片")
    public static class RelatedCard {
        @Schema(description = "卡片ID")
        private Long cardId;

        @Schema(description = "卡片标题")
        private String title;

        @Schema(description = "关联原因")
        private String relevanceReason;
    }

    /**
     * 轮次统计
     */
    @Data
    @Schema(description = "轮次统计")
    public static class RoundStats {
        @Schema(description = "总轮次")
        private Integer totalRounds;

        @Schema(description = "平均回复长度")
        private Integer avgResponseLength;

        @Schema(description = "深度层级分布")
        private List<Integer> depthDistribution;

        @Schema(description = "思考深度评分 1-10")
        private Integer thinkingDepthScore;
    }
}
