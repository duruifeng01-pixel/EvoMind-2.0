package com.evomind.dto.request;

import com.evomind.entity.ContentModerationLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 内容审核请求DTO
 */
@Data
public class ModerationRequest {

    /**
     * 内容类型
     */
    @NotNull(message = "内容类型不能为空")
    private ContentModerationLog.ContentType contentType;

    /**
     * 内容ID（如果已存在）
     */
    private String contentId;

    /**
     * 待审核内容
     */
    @NotBlank(message = "审核内容不能为空")
    @Size(max = 10000, message = "内容长度不能超过10000字符")
    private String content;

    /**
     * 内容摘要（可选，用于日志展示）
     */
    @Size(max = 500, message = "摘要长度不能超过500字符")
    private String contentSummary;

    /**
     * 是否AI生成内容
     */
    private Boolean isAiGenerated = false;

    /**
     * AI模型名称（如果是AI生成）
     */
    private String aiModel;

    /**
     * 是否强制重新审核（即使有缓存结果）
     */
    private Boolean forceReCheck = false;

    /**
     * 审核优先级（true为高优先级）
     */
    private Boolean highPriority = false;
}