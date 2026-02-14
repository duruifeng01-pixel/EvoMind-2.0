package com.evomind.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 结束讨论请求
 */
@Data
@Schema(description = "结束讨论请求")
public class FinalizeDiscussionRequest {

    @Schema(description = "用户个人总结/感悟", example = "通过这次讨论，我认识到AI对人类工作的影响是双面的...")
    private String personalInsight;

    @Schema(description = "是否公开分享", example = "true")
    private Boolean isPublic = false;
}
