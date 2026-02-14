package com.evomind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 链接导入请求
 */
@Data
@Schema(description = "链接导入请求")
public class LinkImportRequest {

    @NotBlank(message = "链接URL不能为空")
    @Pattern(regexp = "^(https?://).+", message = "请输入有效的HTTP/HTTPS链接")
    @Schema(description = "要抓取的链接URL", required = true, example = "https://www.xiaohongshu.com/discovery/item/xxx")
    private String url;

    @Schema(description = "期望的平台类型，用于校验：xiaohongshu/weixin/zhihu/douyin", example = "xiaohongshu")
    private String expectedPlatform;

    @Schema(description = "是否下载图片资源", example = "true")
    private Boolean downloadImages = true;
}
