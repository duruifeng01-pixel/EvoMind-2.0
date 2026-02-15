package com.evomind.dto.response;

import com.evomind.entity.ContentModerationLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容审核响应DTO
 */
@Data
@Builder
public class ModerationResponse {

    /**
     * 审核记录ID
     */
    private Long logId;

    /**
     * 审核状态
     */
    private ContentModerationLog.ModerationStatus status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 是否通过审核
     */
    private Boolean approved;

    /**
     * 是否阻断展示
     */
    private Boolean shouldBlock;

    /**
     * 违规类型
     */
    private ContentModerationLog.ViolationType violationType;

    /**
     * 违规详情
     */
    private String violationDetails;

    /**
     * 命中敏感词列表
     */
    private List<HitWordInfo> hitSensitiveWords;

    /**
     * 审核方式
     */
    private ContentModerationLog.ModerationType moderationType;

    /**
     * 审核服务商
     */
    private String provider;

    /**
     * 是否需要人工复核
     */
    private Boolean needManualReview;

    /**
     * 建议操作
     */
    private SuggestedAction suggestedAction;

    /**
     * 审核时间
     */
    private LocalDateTime moderatedAt;

    /**
     * 处理耗时（毫秒）
     */
    private Long processTimeMs;

    /**
     * 命中的敏感词信息
     */
    @Data
    @Builder
    public static class HitWordInfo {
        /**
         * 敏感词ID
         */
        private Long wordId;

        /**
         * 敏感词内容
         */
        private String word;

        /**
         * 敏感词分类
         */
        private String category;

        /**
         * 敏感级别
         */
        private String level;

        /**
         * 命中的位置（在原文中的索引）
         */
        private List<Position> positions;

        /**
         * 位置信息
         */
        @Data
        @Builder
        public static class Position {
            private int start;
            private int end;
        }
    }

    /**
     * 建议操作枚举
     */
    public enum SuggestedAction {
        ALLOW,           // 允许展示
        BLOCK,           // 阻断展示
        MASK,            // 打码展示
        REVIEW,          // 人工复核
        RETRY            // 重新提交
    }

    /**
     * 创建通过响应
     */
    public static ModerationResponse approved(Long logId, Long processTimeMs) {
        return ModerationResponse.builder()
                .logId(logId)
                .status(ContentModerationLog.ModerationStatus.APPROVED)
                .statusDescription("审核通过")
                .approved(true)
                .shouldBlock(false)
                .suggestedAction(SuggestedAction.ALLOW)
                .moderatedAt(LocalDateTime.now())
                .processTimeMs(processTimeMs)
                .build();
    }

    /**
     * 创建拒绝响应
     */
    public static ModerationResponse rejected(Long logId, ContentModerationLog.ViolationType violationType,
                                               String violationDetails, Long processTimeMs) {
        return ModerationResponse.builder()
                .logId(logId)
                .status(ContentModerationLog.ModerationStatus.REJECTED)
                .statusDescription("审核不通过")
                .approved(false)
                .shouldBlock(true)
                .violationType(violationType)
                .violationDetails(violationDetails)
                .suggestedAction(SuggestedAction.BLOCK)
                .moderatedAt(LocalDateTime.now())
                .processTimeMs(processTimeMs)
                .build();
    }
}