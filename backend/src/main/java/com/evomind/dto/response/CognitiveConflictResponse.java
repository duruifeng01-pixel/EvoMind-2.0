package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 认知冲突响应DTO
 */
@Data
@Builder
@Schema(description = "认知冲突信息（新卡片与用户认知体系的冲突）")
public class CognitiveConflictResponse {

    @Schema(description = "冲突ID")
    private Long id;

    @Schema(description = "卡片ID")
    private Long cardId;

    @Schema(description = "卡片标题")
    private String cardTitle;

    @Schema(description = "卡片核心观点")
    private String cardViewpoint;

    @Schema(description = "认知画像ID")
    private Long profileId;

    @Schema(description = "涉及主题")
    private String topic;

    @Schema(description = "用户原有信念")
    private String userBelief;

    @Schema(description = "冲突类型")
    private String conflictType;

    @Schema(description = "冲突类型描述")
    private String conflictTypeDescription;

    @Schema(description = "冲突描述")
    private String conflictDescription;

    @Schema(description = "冲突分数 (0-1)")
    private BigDecimal conflictScore;

    @Schema(description = "是否已确认")
    private Boolean isAcknowledged;

    @Schema(description = "是否已忽略")
    private Boolean isDismissed;

    @Schema(description = "AI分析结果")
    private String aiAnalysis;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 获取冲突类型描述
     */
    public String getConflictTypeDescription() {
        if (conflictType == null) {
            return "未知类型";
        }
        return switch (conflictType) {
            case "CONTRADICTORY" -> "观点对立";
            case "CHALLENGING" -> "挑战信念";
            case "DIFFERENT_PERSPECTIVE" -> "不同视角";
            case "EXTENDING" -> "延伸拓展";
            default -> "其他";
        };
    }
}
