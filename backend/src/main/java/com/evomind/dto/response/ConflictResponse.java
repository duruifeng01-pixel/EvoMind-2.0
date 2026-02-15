package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 冲突响应DTO
 */
@Data
@Builder
@Schema(description = "观点冲突信息")
public class ConflictResponse {

    @Schema(description = "冲突ID")
    private Long id;

    @Schema(description = "卡片1 ID")
    private Long cardId1;

    @Schema(description = "卡片2 ID")
    private Long cardId2;

    @Schema(description = "卡片1标题")
    private String cardTitle1;

    @Schema(description = "卡片2标题")
    private String cardTitle2;

    @Schema(description = "卡片1核心观点")
    private String cardViewpoint1;

    @Schema(description = "卡片2核心观点")
    private String cardViewpoint2;

    @Schema(description = "冲突类型")
    private String conflictType;

    @Schema(description = "冲突类型描述")
    private String conflictTypeDescription;

    @Schema(description = "冲突描述")
    private String conflictDescription;

    @Schema(description = "涉及主题")
    private String topic;

    @Schema(description = "相似度分数 (0-1)")
    private BigDecimal similarityScore;

    @Schema(description = "冲突分数 (0-1)")
    private BigDecimal conflictScore;

    @Schema(description = "是否已确认")
    private Boolean isAcknowledged;

    @Schema(description = "AI分析结果")
    private String aiAnalysis;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "确认时间")
    private LocalDateTime acknowledgedAt;

    /**
     * 获取冲突类型描述
     */
    public String getConflictTypeDescription() {
        if (conflictType == null) {
            return "未知类型";
        }
        return switch (conflictType) {
            case "CONTRADICTORY" -> "观点对立";
            case "COMPLEMENTARY" -> "观点互补";
            case "DIFFERENT_PERSPECTIVE" -> "不同视角";
            case "TOPIC_OVERLAP" -> "主题重叠";
            default -> "其他";
        };
    }
}
