package com.evomind.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 语音笔记创建/更新请求DTO
 */
@Data
public class VoiceNoteRequest {

    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    @Size(max = 500, message = "标签长度不能超过500字符")
    private String tags;

    private Boolean isFavorite;

    private Boolean isArchived;

    /**
     * 转写文本（手动编辑时使用）
     */
    @Size(max = 4000, message = "转写文本长度不能超过4000字符")
    private String transcribedText;
}
