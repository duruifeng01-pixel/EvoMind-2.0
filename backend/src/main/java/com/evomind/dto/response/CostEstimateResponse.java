package com.evomind.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订阅费用预估响应DTO
 */
@Data
@Builder
public class CostEstimateResponse {

    // 预估基于的时间范围
    private String estimateBasedOn;

    // 当前使用统计
    private CurrentUsageDTO currentUsage;

    // 预估结果
    private EstimateResultDTO estimate;

    // 单价明细
    private List<UnitPriceDTO> unitPrices;

    // 计算公式
    private String formula;

    @Data
    @Builder
    public static class CurrentUsageDTO {
        private Integer sourceCount;           // 当前信息源数量
        private Integer conflictMarkCount;     // 冲突标记次数
        private Integer ocrRequestCount;       // OCR请求次数
        private Long aiTokenCount;             // AI Token消耗
        private Integer dialogueTurnCount;     // 对话轮数
        private Integer modelTrainingCount;    // 模型训练次数
        private Integer feedCardCount;         // 信息流卡片数
    }

    @Data
    @Builder
    public static class EstimateResultDTO {
        private BigDecimal dailyCost;         // 日成本
        private BigDecimal monthlyCost;       // 月成本预估
        private BigDecimal dailySubscription; // 日订阅费
        private BigDecimal monthlySubscription; // 月订阅费预估
        private Integer costMultiplier;       // 成本倍数
    }

    @Data
    @Builder
    public static class UnitPriceDTO {
        private String name;                  // 项目名称
        private String code;                  // 代码
        private BigDecimal price;             // 单价
        private String unit;                  // 单位
        private String category;              // 类别
    }
}
