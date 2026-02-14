package com.evomind.service;

import com.evomind.entity.AiCallLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AiLogService {

    void logAiCall(Long userId, String sceneCode, String modelName, 
                   Integer tokenIn, Integer tokenOut, Integer latencyMs, 
                   BigDecimal costAmount, boolean success, String errorMsg);

    List<AiCallLog> getLogsByUserId(Long userId);

    List<AiCallLog> getLogsByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end);

    Integer getTotalTokenIn(Long userId, LocalDateTime start, LocalDateTime end);

    Integer getTotalTokenOut(Long userId, LocalDateTime start, LocalDateTime end);

    BigDecimal getTotalCost(Long userId, LocalDateTime start, LocalDateTime end);

    Long getFailedCallCount(Long userId, LocalDateTime start, LocalDateTime end);

    Map<String, Long> getSceneCodeStats(Long userId, LocalDateTime start, LocalDateTime end);
}
