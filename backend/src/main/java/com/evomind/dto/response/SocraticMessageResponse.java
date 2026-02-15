package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 苏格拉底式对话消息响应
 */
@Data
@Schema(description = "苏格拉底式对话消息响应")
public class SocraticMessageResponse {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "对话ID")
    private Long dialogueId;

    @Schema(description = "轮次")
    private Integer round;

    @Schema(description = "角色: AI/USER")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型")
    private String type;

    @Schema(description = "类型描述")
    private String typeDescription;

    @Schema(description = "深度层级")
    private Integer depthLevel;

    @Schema(description = "是否是追问")
    private Boolean isFollowUp;

    @Schema(description = "思考提示（仅AI消息）")
    private String thinkingHints;

    @Schema(description = "AI分析（仅AI消息）")
    private String aiAnalysis;

    @Schema(description = "提取的关键点（仅用户消息）")
    private String keyPointsExtracted;

    @Schema(description = "是否是最终总结")
    private Boolean isFinalSummary;

    @Schema(description = "序号")
    private Integer sequenceNumber;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "追问选项（AI问题时提供）")
    private List<FollowUpOption> followUpOptions;

    /**
     * 追问选项
     */
    @Data
    @Schema(description = "追问选项")
    public static class FollowUpOption {
        @Schema(description = "选项ID")
        private String id;

        @Schema(description = "选项标签")
        private String label;

        @Schema(description = "选项类型")
        private String type;
    }
}
