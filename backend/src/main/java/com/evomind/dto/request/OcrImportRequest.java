package com.evomind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OCR截图导入请求
 */
@Data
@Schema(description = "OCR截图导入请求")
public class OcrImportRequest {

    @NotBlank(message = "图片Base64不能为空")
    @Schema(description = "截图图片的Base64编码", required = true)
    private String imageBase64;

    @Schema(description = "图片格式：jpeg/png，默认jpeg", example = "jpeg")
    private String imageFormat = "jpeg";

    @Schema(description = "图片裁剪区域：x,y,width,height，用于聚焦特定区域")
    private String cropRegion;
}
