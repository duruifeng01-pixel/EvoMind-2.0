package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应DTO
 */
@Data
@Schema(description = "评论响应")
public class CommentResponse {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "讨论ID")
    private Long discussionId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "回复给哪个用户ID")
    private Long replyToUserId;

    @Schema(description = "回复给哪个用户昵称")
    private String replyToUserNickname;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "是否置顶")
    private Boolean isTop;

    @Schema(description = "当前用户是否点赞")
    private Boolean hasLiked;

    @Schema(description = "回复列表")
    private List<CommentResponse> replies;

    @Schema(description = "回复数")
    private Integer replyCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
