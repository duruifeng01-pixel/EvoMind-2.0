package com.evomind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提交作品请求
 */
@Data
@Schema(description = "提交作品请求")
public class SubmitArtifactRequest {

    @NotBlank(message = "作品标题不能为空")
    @Schema(description = "作品标题/名称", example = "我的AI工作观总结", required = true)
    private String title;

    @Schema(description = "作品类型", example = "NOTE")
    private String type;

    @Schema(description = "作品内容/描述", example = "通过今日讨论，我整理了关于AI影响工作的三点思考...")
    private String content;

    @Schema(description = "关联的认知卡片ID列表", example = "[1, 2, 3]")
    private java.util.List<Long> relatedCardIds;

    @Schema(description = "外部链接（如分享链接）", example = "https://...")
    private String externalUrl;
}
