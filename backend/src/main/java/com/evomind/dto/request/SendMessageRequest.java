package com.evomind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送苏格拉底式对话消息请求
 */
@Data
@Schema(description = "发送苏格拉底式对话消息请求")
public class SendMessageRequest {

    @NotNull(message = "对话ID不能为空")
    @Schema(description = "对话会话ID", required = true, example = "1")
    private Long dialogueId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户回复内容", required = true, example = "我认为这个问题的核心在于...")
    private String content;

    @Schema(description = "追问深度（可选，1-5）", example = "3")
    private Integer requestedDepth;

    @Schema(description = "是否请求结束对话并生成洞察", example = "false")
    private Boolean requestFinalize = false;

    @Schema(description = "用户满意度评分（结束对话时）1-5", example = "4")
    private Integer satisfaction;
}
