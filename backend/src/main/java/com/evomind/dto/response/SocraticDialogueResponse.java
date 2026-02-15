package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 苏格拉底式对话会话响应
 */
@Data
@Schema(description = "苏格拉底式对话会话响应")
public class SocraticDialogueResponse {

    @Schema(description = "对话ID")
    private Long id;

    @Schema(description = "讨论ID")
    private Long discussionId;

    @Schema(description = "讨论标题")
    private String discussionTitle;

    @Schema(description = "对话状态")
    private String status;

    @Schema(description = "当前轮次")
    private Integer currentRound;

    @Schema(description = "最大轮次")
    private Integer maxRounds;

    @Schema(description = "初始问题")
    private String initialQuestion;

    @Schema(description = "最终洞察")
    private String finalInsight;

    @Schema(description = "总消息数")
    private Integer totalMessages;

    @Schema(description = "是否可以继续对话")
    private Boolean canContinue;

    @Schema(description = "剩余轮次")
    private Integer remainingRounds;

    @Schema(description = "最后活动时间")
    private LocalDateTime lastMessageAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "最新消息预览")
    private SocraticMessageResponse lastMessage;
}
