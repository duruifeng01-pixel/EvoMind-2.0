package com.evomind.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OCR识别结果响应DTO
 */
@Data
@Builder
@Schema(description = "OCR识别结果")
public class OcrResultResponse {

    @Schema(description = "识别任务ID")
    private String taskId;

    @Schema(description = "识别的博主/账号列表")
    private List<DetectedBlogger> bloggers;

    @Schema(description = "识别的原始文本块")
    private List<TextBlock> textBlocks;

    @Schema(description = "识别状态", example = "SUCCESS")
    private String status;

    @Schema(description = "处理耗时(毫秒)")
    private Long processingTimeMs;

    @Schema(description = "识别时间")
    private LocalDateTime recognizedAt;

    @Schema(description = "是否需要手动确认", example = "true")
    private Boolean needsConfirmation;

    @Data
    @Builder
    @Schema(description = "检测到的博主信息")
    public static class DetectedBlogger {
        @Schema(description = "候选ID")
        private String candidateId;

        @Schema(description = "博主名称")
        private String name;

        @Schema(description = "头像URL(如识别到)")
        private String avatarUrl;

        @Schema(description = "平台", example = "xiaohongshu")
        private String platform;

        @Schema(description = "主页链接(如可推断)")
        private String homeUrl;

        @Schema(description = "置信度(0-1)", example = "0.92")
        private Double confidence;

        @Schema(description = "是否已存在于用户源", example = "false")
        private Boolean alreadyExists;

        @Schema(description = "源ID(如已存在)")
        private Long existingSourceId;

        @Schema(description = "相关文本位置信息")
        private BoundingBox boundingBox;
    }

    @Data
    @Builder
    @Schema(description = "文本块信息")
    public static class TextBlock {
        @Schema(description = "文本内容")
        private String text;

        @Schema(description = "位置信息")
        private BoundingBox boundingBox;

        @Schema(description = "置信度", example = "0.95")
        private Double confidence;
    }

    @Data
    @Builder
    @Schema(description = "边界框位置信息")
    public static class BoundingBox {
        @Schema(description = "左上角X坐标")
        private Integer x;

        @Schema(description = "左上角Y坐标")
        private Integer y;

        @Schema(description = "宽度")
        private Integer width;

        @Schema(description = "高度")
        private Integer height;
    }
}
