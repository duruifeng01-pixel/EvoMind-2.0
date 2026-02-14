package com.evomind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OCR导入请求DTO
 * 支持Base64编码的图片或图片URL
 */
@Data
@Schema(description = "OCR导入请求")
public class OcrImportRequest {

    @NotBlank(message = "图片Base64数据不能为空")
    @Schema(description = "图片Base64编码数据(不含data URL前缀)", required = true, example = "/9j/4AAQ...")
    private String imageBase64;

    @Schema(description = "图片格式", example = "jpeg", allowableValues = {"jpeg", "jpg", "png", "webp"})
    private String imageFormat = "jpeg";

    @Schema(description = "图片来源平台", example = "xiaohongshu", allowableValues = {"xiaohongshu", "weixin", "douyin", "zhihu", "other"})
    private String platform;

    @Schema(description = "是否预处理图片(裁剪/旋转)", example = "true")
    private Boolean preprocessImage = true;

    @Schema(description = "图片宽度", example = "1080")
    private Integer imageWidth;

    @Schema(description = "图片高度", example = "1920")
    private Integer imageHeight;

    /**
     * 获取完整Data URL格式
     */
    public String getImageData() {
        String format = imageFormat != null ? imageFormat.toLowerCase() : "jpeg";
        if (format.equals("jpg")) format = "jpeg";
        return "data:image/" + format + ";base64," + imageBase64;
    }
}
