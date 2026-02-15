package com.evomind.dto.response;

import com.evomind.entity.SensitiveWord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词响应DTO
 */
@Data
@Builder
public class SensitiveWordResponse {

    /**
     * 敏感词ID
     */
    private Long id;

    /**
     * 敏感词内容
     */
    private String word;

    /**
     * 敏感词分类
     */
    private String category;

    private String categoryLabel;

    /**
     * 敏感级别
     */
    private String level;

    private String levelLabel;

    /**
     * 匹配模式
     */
    private String matchMode;

    private String matchModeLabel;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 来源
     */
    private String source;

    /**
     * 命中次数
     */
    private Long hitCount;

    /**
     * 最后命中时间
     */
    private LocalDateTime lastHitAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 从实体转换
     */
    public static SensitiveWordResponse fromEntity(SensitiveWord entity) {
        return SensitiveWordResponse.builder()
                .id(entity.getId())
                .word(entity.getWord())
                .category(entity.getCategory().name())
                .categoryLabel(entity.getCategory().getLabel())
                .level(entity.getLevel().name())
                .levelLabel(entity.getLevel().getLabel())
                .matchMode(entity.getMatchMode().name())
                .matchModeLabel(entity.getMatchMode().getLabel())
                .enabled(entity.getEnabled())
                .description(entity.getDescription())
                .source(entity.getSource() != null ? entity.getSource().name() : null)
                .hitCount(entity.getHitCount())
                .lastHitAt(entity.getLastHitAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}