package com.evomind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 确认导入OCR识别结果请求DTO
 */
@Data
@Schema(description = "确认OCR导入请求")
public class ConfirmImportRequest {

    @NotEmpty(message = "至少选择一个要导入的博主")
    @Schema(description = "选中的博主候选ID列表", required = true)
    private List<String> selectedCandidateIds;

    @Schema(description = "OCR任务ID")
    private String taskId;

    @Schema(description = "是否全部选中", example = "false")
    private Boolean selectAll = false;

    @Schema(description = "自定义名称映射(候选ID -> 自定义名称)")
    private java.util.Map<String, String> customNames;
}
