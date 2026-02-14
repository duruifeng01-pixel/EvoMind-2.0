package com.evomind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 确认导入请求（选择要添加的信息源）
 */
@Data
@Schema(description = "确认导入请求")
public class ConfirmImportRequest {

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "导入任务ID", required = true)
    private Long jobId;

    @NotEmpty(message = "至少要选择一个作者")
    @Schema(description = "选中的作者列表", required = true)
    private List<SelectedAuthor> selectedAuthors;

    @Data
    @Schema(description = "选中的作者信息")
    public static class SelectedAuthor {

        @Schema(description = "作者名称", required = true)
        private String name;

        @Schema(description = "作者头像URL")
        private String avatarUrl;

        @Schema(description = "主页链接")
        private String homeUrl;

        @Schema(description = "平台类型", example = "xiaohongshu")
        private String platform;

        @Schema(description = "分类标签")
        private String category;
    }
}
