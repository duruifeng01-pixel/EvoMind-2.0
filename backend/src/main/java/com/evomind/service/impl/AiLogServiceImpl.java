package com.evomind.service.impl;

import com.evomind.entity.AiCallLog;
import com.evomind.repository.AiCallLogRepository;
import com.evomind.service.AiLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiLogServiceImpl implements AiLogService {

    private final AiCallLogRepository aiCallLogRepository;

    @Override
    @Transactional
    public void logAiCall(Long userId, String sceneCode, String modelName, 
                         Integer tokenIn, Integer tokenOut, Integer latencyMs, 
                         BigDecimal costAmount, boolean success, String errorMsg) {
        AiCallLog log = new AiCallLog();
        log.setUserId(userId);
        log.setSceneCode(sceneCode);
        log.setModelName(modelName);
        log.setTokenIn(tokenIn != null ? tokenIn : 0);
        log.setTokenOut(tokenOut != null ? tokenOut : 0);
        log.setLatencyMs(latencyMs != null ? latencyMs : 0);
        log.setCostAmount(costAmount != null ? costAmount : BigDecimal.ZERO);
        log.setSuccess(success);
        log.setErrorMsg(errorMsg);
        
        aiCallLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiCallLog> getLogsByUserId(Long userId) {
        return aiCallLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiCallLog> getLogsByUserIdAndTimeRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return aiCallLogRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalTokenIn(Long userId, LocalDateTime start, LocalDateTime end) {
        Integer result = aiCallLogRepository.sumTokenInByUserAndTimeRange(userId, start, end);
        return result != null ? result : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalTokenOut(Long userId, LocalDateTime start, LocalDateTime end) {
        Integer result = aiCallLogRepository.sumTokenOutByUserAndTimeRange(userId, start, end);
        return result != null ? result : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(Long userId, LocalDateTime start, LocalDateTime end) {
        BigDecimal result = aiCallLogRepository.sumCostByUserAndTimeRange(userId, start, end);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getFailedCallCount(Long userId, LocalDateTime start, LocalDateTime end) {
        return aiCallLogRepository.countFailedCallsByUserAndTimeRange(userId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getSceneCodeStats(Long userId, LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = aiCallLogRepository.countBySceneCode(userId, start, end);
        Map<String, Long> stats = new HashMap<>();
        for (Object[] result : results) {
            String sceneCode = (String) result[0];
            Long count = (Long) result[1];
            stats.put(sceneCode, count);
        }
        return stats;
    }
}
