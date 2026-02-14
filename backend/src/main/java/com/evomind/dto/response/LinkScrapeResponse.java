package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 链接抓取响应DTO
 */
@Data
@Builder
@Schema(description = "链接抓取结果")
public class LinkScrapeResponse {

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "抓取状态")
    private String status;

    @Schema(description = "链接URL")
    private String url;

    @Schema(description = "平台类型")
    private String platform;

    @Schema(description = "内容标题")
    private String title;

    @Schema(description = "作者名称")
    private String author;

    @Schema(description = "作者头像")
    private String authorAvatar;

    @Schema(description = "正文内容（HTML或纯文本）")
    private String content;

    @Schema(description = "内容摘要")
    private String summary;

    @Schema(description = "图片列表")
    private List<ImageInfo> images;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "抓取时间")
    private LocalDateTime scrapedAt;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "处理耗时(毫秒)")
    private Long processingTimeMs;

    @Data
    @Builder
    @Schema(description = "图片信息")
    public static class ImageInfo {
        @Schema(description = "图片URL")
        private String url;

        @Schema(description = "图片描述")
        private String description;

        @Schema(description = "宽度")
        private Integer width;

        @Schema(description = "高度")
        private Integer height;

        @Schema(description = "是否已下载到本地")
        private Boolean downloaded;
    }
}
