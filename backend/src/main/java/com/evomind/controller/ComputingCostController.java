package com.evomind.controller;

import com.evomind.dto.response.ApiResponse;
import com.evomind.dto.response.ComputingCostStatsResponse;
import com.evomind.dto.response.CostEstimateResponse;
import com.evomind.dto.response.CostUnitPriceResponse;
import com.evomind.security.UserDetailsImpl;
import com.evomind.service.CostStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 算力成本统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/computing-cost")
@RequiredArgsConstructor
@Tag(name = "算力成本统计", description = "用户算力成本统计与订阅费预估相关接口")
public class ComputingCostController {

    private final CostStatisticsService costStatisticsService;

    @GetMapping("/stats")
    @Operation(summary = "获取算力成本统计", description = "获取用户在指定时间范围内的算力成本统计，默认最近30天")
    public ApiResponse<ComputingCostStatsResponse> getComputingCostStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = userDetails.getId();
        ComputingCostStatsResponse response;

        if (startDate != null && endDate != null) {
            response = costStatisticsService.getComputingCostStats(userId, startDate, endDate);
        } else {
            response = costStatisticsService.getComputingCostStats(userId);
        }

        return ApiResponse.success(response);
    }

    @GetMapping("/estimate")
    @Operation(summary = "预估订阅费用", description = "基于用户最近7天的使用数据预估月订阅费用")
    public ApiResponse<CostEstimateResponse> estimateSubscriptionFee(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long userId = userDetails.getId();
        CostEstimateResponse response = costStatisticsService.estimateSubscriptionFee(userId);

        return ApiResponse.success(response);
    }

    @GetMapping("/unit-prices")
    @Operation(summary = "获取成本单价配置", description = "获取当前生效的各项AI服务成本单价")
    public ApiResponse<List<CostUnitPriceResponse>> getCostUnitPrices() {
        List<CostUnitPriceResponse> prices = costStatisticsService.getCostUnitPrices();
        return ApiResponse.success(prices);
    }

    @PostMapping("/recalculate")
    @Operation(summary = "重新计算今日成本", description = "手动触发重新计算今日算力成本（仅用于调试）")
    public ApiResponse<Void> recalculateTodayCost(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long userId = userDetails.getId();
        costStatisticsService.recordCost(userId, LocalDate.now());

        return ApiResponse.success("成本重新计算完成", null);
    }
}
