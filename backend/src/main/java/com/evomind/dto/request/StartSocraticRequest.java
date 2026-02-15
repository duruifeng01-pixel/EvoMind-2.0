package com.evomind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始苏格拉底式对话请求
 */
@Data
@Schema(description = "开始苏格拉底式对话请求")
public class StartSocraticRequest {

    @NotNull(message = "讨论ID不能为空")
    @Schema(description = "讨论主题ID", required = true, example = "1")
    private Long discussionId;

    @Schema(description = "最大对话轮次（可选，默认5轮）", example = "5")
    private Integer maxRounds = 5;

    @Schema(description = "用户初始观点（可选）", example = "我认为人工智能会取代很多重复性工作")
    private String initialThought;

    @Schema(description = "对话主题（可选，覆盖讨论主题）")
    private String topicOverride;
}
