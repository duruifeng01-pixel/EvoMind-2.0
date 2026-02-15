package com.evomind.service;

import com.evomind.dto.response.ComputingCostStatsResponse;
import com.evomind.dto.response.CostEstimateResponse;
import com.evomind.dto.response.CostUnitPriceResponse;
import com.evomind.entity.ComputingCostRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 成本统计服务接口
 */
public interface CostStatisticsService {

    /**
     * 获取用户算力成本统计
     */
    ComputingCostStatsResponse getComputingCostStats(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取用户算力成本统计（默认最近30天）
     */
    ComputingCostStatsResponse getComputingCostStats(Long userId);

    /**
     * 预估订阅费用
     */
    CostEstimateResponse estimateSubscriptionFee(Long userId);

    /**
     * 获取成本单价配置列表
     */
    List<CostUnitPriceResponse> getCostUnitPrices();

    /**
     * 记录或更新用户成本
     */
    ComputingCostRecord recordCost(Long userId, LocalDate recordDate);

    /**
     * 增加OCR请求成本
     */
    void incrementOcrCost(Long userId);

    /**
     * 增加AI Token消耗
     */
    void incrementAiTokenCost(Long userId, Long tokenCount);

    /**
     * 增加对话轮数成本
     */
    void incrementDialogueCost(Long userId);

    /**
     * 增加内容抓取成本
     */
    void incrementCrawlCost(Long userId);

    /**
     * 更新信息源数量成本
     */
    void updateSourceCountCost(Long userId);

    /**
     * 更新冲突标记成本
     */
    void updateConflictMarkCost(Long userId);

    /**
     * 计算订阅费用
     * 公式：订阅费 = 总成本 × 成本倍数（默认2倍）
     */
    BigDecimal calculateSubscriptionFee(BigDecimal totalCost, Integer multiplier);

    /**
     * 定时任务：更新所有用户的成本统计
     */
    void updateAllUsersCostStatistics();
}
