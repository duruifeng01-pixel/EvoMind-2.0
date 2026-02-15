package com.evomind.service.impl;

import com.evomind.config.DeepSeekConfig;
import com.evomind.entity.Card;
import com.evomind.service.AiLogService;
import com.evomind.service.OpinionAnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 观点分析服务实现
 * 集成DeepSeek AI进行观点冲突检测
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpinionAnalysisServiceImpl implements OpinionAnalysisService {

    private final DeepSeekConfig deepSeekConfig;
    private final AiLogService aiLogService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OpinionConflictResult analyzeConflict(Card card1, Card card2) {
        log.info("开始分析卡片冲突: card1={}, card2={}", card1.getId(), card2.getId());

        try {
            // 构建Prompt
            String prompt = buildConflictDetectionPrompt(card1, card2);
            
            // 调用DeepSeek API
            String aiResponse = callDeepSeekApi(prompt);
            
            // 解析AI响应
            OpinionConflictResult result = parseConflictResponse(aiResponse, card1, card2);
            
            // 记录AI调用日志
            aiLogService.logAiCall(
                "CONFLICT_DETECTION",
                prompt,
                aiResponse,
                result.isHasConflict() ? 1 : 0,
                null,
                0L
            );
            
            return result;
            
        } catch (Exception e) {
            log.error("观点冲突分析失败", e);
            // 返回无冲突结果
            OpinionConflictResult result = new OpinionConflictResult();
            result.setHasConflict(false);
            result.setCard1(card1);
            result.setCard2(card2);
            result.setAiAnalysis("分析失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public List<OpinionConflictResult> analyzeBatchConflicts(Card targetCard, List<Card> candidateCards) {
        List<OpinionConflictResult> results = new ArrayList<>();
        
        for (Card candidateCard : candidateCards) {
            if (!candidateCard.getId().equals(targetCard.getId())) {
                OpinionConflictResult result = analyzeConflict(targetCard, candidateCard);
                if (result.isHasConflict()) {
                    results.add(result);
                }
            }
        }
        
        return results;
    }

    @Override
    public OpinionStance extractOpinionStance(Card card) {
        OpinionStance stance = new OpinionStance();
        stance.setMainTopic(card.getKeywords());
        stance.setCoreViewpoint(card.getOneSentenceSummary());
        stance.setKeyClaims(Arrays.asList(card.getKeyPoints().split("\\n")));
        return stance;
    }

    /**
     * 构建冲突检测Prompt
     */
    private String buildConflictDetectionPrompt(Card card1, Card card2) {
        return String.format("""
            请分析以下两张认知卡片是否存在观点冲突或对立。
            
            ## 卡片1
            标题: %s
            核心观点: %s
            摘要: %s
            关键词: %s
            
            ## 卡片2
            标题: %s
            核心观点: %s
            摘要: %s
            关键词: %s
            
            ## 分析要求
            请判断这两张卡片在主题或观点上是否存在冲突、对立或相反的立场。
            如果是相似观点、补充说明或无关内容，不算冲突。
            
            请按以下JSON格式输出：
            {
                "hasConflict": true/false,
                "conflictType": "CONTRADICTORY|COMPLEMENTARY|DIFFERENT_PERSPECTIVE|NONE",
                "topic": "冲突涉及的主题",
                "conflictScore": 0.0-1.0,
                "description": "简要描述冲突内容",
                "analysis": "详细的AI分析说明"
            }
            
            只输出JSON，不要其他解释。
            """,
            card1.getTitle(),
            card1.getOneSentenceSummary(),
            card1.getKeyPoints(),
            card1.getKeywords(),
            card2.getTitle(),
            card2.getOneSentenceSummary(),
            card2.getKeyPoints(),
            card2.getKeywords()
        );
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekApi(String prompt) {
        if (!deepSeekConfig.isEnabled()) {
            // 模拟模式
            return simulateConflictDetection();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepSeekConfig.getApiKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", deepSeekConfig.getModel());
        requestBody.put("messages", List.of(
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 500);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            deepSeekConfig.getApiUrl(),
            request,
            String.class
        );

        // 解析响应
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("解析DeepSeek响应失败", e);
            return simulateConflictDetection();
        }
    }

    /**
     * 解析AI冲突检测响应
     */
    private OpinionConflictResult parseConflictResponse(String aiResponse, Card card1, Card card2) {
        OpinionConflictResult result = new OpinionConflictResult();
        result.setCard1(card1);
        result.setCard2(card2);

        try {
            // 提取JSON
            String json = extractJson(aiResponse);
            JsonNode root = objectMapper.readTree(json);
            
            result.setHasConflict(root.path("hasConflict").asBoolean(false));
            result.setConflictType(root.path("conflictType").asText("NONE"));
            result.setTopic(root.path("topic").asText("未知主题"));
            result.setConflictDescription(root.path("description").asText(""));
            result.setAiAnalysis(root.path("analysis").asText(""));
            
            double score = root.path("conflictScore").asDouble(0.0);
            result.setConflictScore(BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP));
            
        } catch (Exception e) {
            log.error("解析冲突响应失败: {}", aiResponse, e);
            result.setHasConflict(false);
            result.setAiAnalysis("解析失败");
            result.setConflictScore(BigDecimal.ZERO);
        }

        return result;
    }

    /**
     * 从文本中提取JSON
     */
    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /**
     * 模拟冲突检测结果（当API不可用时）
     */
    private String simulateConflictDetection() {
        // 模拟响应 - 随机决定是否冲突
        boolean hasConflict = Math.random() > 0.7;
        
        if (hasConflict) {
            return """
                {
                    "hasConflict": true,
                    "conflictType": "DIFFERENT_PERSPECTIVE",
                    "topic": "认知与成长",
                    "conflictScore": 0.75,
                    "description": "两张卡片对同一主题提出了不同角度的观点",
                    "analysis": "卡片1强调系统性思维的重要性，而卡片2更关注直觉决策。这代表了理性与感性两种不同认知路径。"
                }
                """;
        } else {
            return """
                {
                    "hasConflict": false,
                    "conflictType": "NONE",
                    "topic": "",
                    "conflictScore": 0.15,
                    "description": "",
                    "analysis": "两张卡片观点互补，不存在冲突。"
                }
                """;
        }
    }
}
