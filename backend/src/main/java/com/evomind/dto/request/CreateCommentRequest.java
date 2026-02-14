package com.evomind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建评论请求
 */
@Data
@Schema(description = "创建评论请求")
public class CreateCommentRequest {

    @NotNull(message = "讨论ID不能为空")
    @Schema(description = "讨论ID", required = true)
    private Long discussionId;

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容", required = true, maxLength = 2000)
    private String content;

    @Schema(description = "回复的评论ID（一级评论不传）")
    private Long parentId;

    @Schema(description = "回复给哪个用户ID")
    private Long replyToUserId;
}
