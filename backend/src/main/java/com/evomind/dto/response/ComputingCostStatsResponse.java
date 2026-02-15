package com.evomind.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 算力成本统计响应DTO
 */
@Data
@Builder
public class ComputingCostStatsResponse {

    // 时间范围
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer days;

    // 成本指标统计
    private MetricsDTO metrics;

    // 成本金额统计
    private CostAmountDTO costAmount;

    // 订阅费用
    private SubscriptionDTO subscription;

    // 历史记录（最近30天）
    private List<DailyRecordDTO> dailyRecords;

    // 计算公式说明
    private String formulaDescription;

    @Data
    @Builder
    public static class MetricsDTO {
        private Integer sourceCount;           // 信息源数量
        private Integer conflictMarkCount;     // 冲突标记次数
        private Integer ocrRequestCount;       // OCR请求次数
        private Long aiTokenCount;             // AI Token消耗数
        private Integer dialogueTurnCount;     // 对话轮数
        private Integer modelTrainingCount;    // 模型训练次数
        private Integer feedCardCount;         // 信息流卡片数
        private Integer crawlRequestCount;     // 内容抓取请求数
    }

    @Data
    @Builder
    public static class CostAmountDTO {
        private BigDecimal ocrCost;           // OCR成本
        private BigDecimal aiCost;            // AI成本
        private BigDecimal crawlCost;         // 抓取成本
        private BigDecimal storageCost;       // 存储成本
        private BigDecimal totalCost;         // 总成本
    }

    @Data
    @Builder
    public static class SubscriptionDTO {
        private BigDecimal costAmount;        // 成本金额
        private Integer costMultiplier;       // 成本倍数（默认2倍）
        private BigDecimal subscriptionFee;   // 订阅费用 = 成本 × 倍数
        private String pricingModel;          // 定价模式说明
    }

    @Data
    @Builder
    public static class DailyRecordDTO {
        private LocalDate date;
        private BigDecimal totalCost;
        private BigDecimal subscriptionFee;
        private Integer sourceCount;
        private Long aiTokenCount;
    }
}
