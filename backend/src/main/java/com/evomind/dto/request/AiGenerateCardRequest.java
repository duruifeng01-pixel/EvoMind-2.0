package com.evomind.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI生成认知卡片请求
 */
@Data
public class AiGenerateCardRequest {
    
    /**
     * 原文内容
     */
    @NotBlank(message = "原文内容不能为空")
    private String originalContent;
    
    /**
     * 内容标题
     */
    private String title;
    
    /**
     * 信息源ID
     */
    private Long sourceId;
    
    /**
     * 来源URL
     */
    private String sourceUrl;
    
    /**
     * 内容类型：article, note, link, voice
     */
    private String contentType = "article";
    
    /**
     * 是否自动生成脑图
     */
    private Boolean generateMindMap = true;
}
