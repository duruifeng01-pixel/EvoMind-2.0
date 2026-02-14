package com.evomind.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCardRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Size(max = 2000, message = "摘要长度不能超过2000个字符")
    private String summaryText;

    @Size(max = 200, message = "一句话导读长度不能超过200个字符")
    private String oneSentenceSummary;

    private Long sourceId;

    @Size(max = 512, message = "来源URL长度不能超过512个字符")
    private String sourceUrl;

    @Size(max = 200, message = "来源标题长度不能超过200个字符")
    private String sourceTitle;

    private String mindmapJson;

    private String keywords;

    private Integer readingTimeMinutes;
}
