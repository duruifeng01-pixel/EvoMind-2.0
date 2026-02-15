package com.evomind.service.impl;

import com.evomind.config.DeepSeekConfig;
import com.evomind.entity.Card;
import com.evomind.entity.UserCognitiveProfile;
import com.evomind.repository.CardRepository;
import com.evomind.repository.UserCognitiveProfileRepository;
import com.evomind.service.AiLogService;
import com.evomind.service.CognitiveProfileService;
import com.evomind.service.TextSimilarityService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 用户认知画像服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CognitiveProfileServiceImpl implements CognitiveProfileService {

    private final UserCognitiveProfileRepository profileRepository;
    private final CardRepository cardRepository;
    private final TextSimilarityService textSimilarityService;
    private final DeepSeekConfig deepSeekConfig;
    private final AiLogService aiLogService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 主题匹配阈值
    private static final double TOPIC_MATCH_THRESHOLD = 0.6;
    // 冲突检测阈值
    private static final double CONFLICT_THRESHOLD = 0.7;

    @Override
    @Transactional
    public void buildCognitiveProfile(Long userId) {
        log.info("开始构建用户认知画像: userId={}", userId);
        
        // 获取用户所有卡片
        List<Card> userCards = cardRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        if (userCards.isEmpty()) {
            log.info("用户没有卡片，跳过构建认知画像");
            return;
        }
        
        // 按主题聚类卡片
        Map<String, List<Card>> topicGroups = clusterCardsByTopic(userCards);
        
        // 为每个主题构建认知画像
        for (Map.Entry<String, List<Card>> entry : topicGroups.entrySet()) {
            String topic = entry.getKey();
            List<Card> cards = entry.getValue();
            
            buildProfileForTopic(userId, topic, cards);
        }
        
        log.info("用户认知画像构建完成: userId={}, 主题数={}", userId, topicGroups.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCognitiveProfile> getUserProfiles(Long userId) {
        return profileRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserCognitiveProfile getProfileByTopic(Long userId, String topic) {
        return profileRepository.findByUserIdAndTopic(userId, topic).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CognitiveConflictResult> checkConflictWithProfile(Long userId, Card card) {
        log.info("检查卡片与用户认知画像冲突: userId={}, cardId={}", userId, card.getId());
        
        List<CognitiveConflictResult> conflicts = new ArrayList<>();
        
        // 获取用户所有认知画像
        List<UserCognitiveProfile> profiles = getUserProfiles(userId);
        
        // 提取卡片主题
        String[] cardTopics = extractTopicsFromCard(card);
        
        for (UserCognitiveProfile profile : profiles) {
            // 检查主题是否相关
            if (isTopicRelated(cardTopics, profile.getTopic())) {
                // 使用AI分析是否存在冲突
                CognitiveConflictResult result = analyzeConflictWithAI(profile, card);
                
                if (result.isHasConflict() && result.getConflictScore() >= CONFLICT_THRESHOLD) {
                    conflicts.add(result);
                }
            }
        }
        
        log.info("检测到 {} 个认知冲突", conflicts.size());
        return conflicts;
    }

    @Override
    @Transactional
    public void updateProfileWithCard(Long userId, Card card) {
        log.info("更新认知画像: userId={}, cardId={}", userId, card.getId());
        
        // 提取卡片主题
        String[] topics = extractTopicsFromCard(card);
        
        for (String topic : topics) {
            Optional<UserCognitiveProfile> existingProfile = 
                profileRepository.findByUserIdAndTopic(userId, topic);
            
            if (existingProfile.isPresent()) {
                // 更新现有画像
                updateExistingProfile(existingProfile.get(), card);
            } else {
                // 创建新画像
                createNewProfile(userId, topic, card);
            }
        }
    }

    /**
     * 按主题聚类卡片
     */
    private Map<String, List<Card>> clusterCardsByTopic(List<Card> cards) {
        Map<String, List<Card>> groups = new HashMap<>();
        
        for (Card card : cards) {
            String[] topics = extractTopicsFromCard(card);
            for (String topic : topics) {
                groups.computeIfAbsent(topic, k -> new ArrayList<>()).add(card);
            }
        }
        
        return groups;
    }

    /**
     * 为主题构建认知画像
     */
    private void buildProfileForTopic(Long userId, String topic, List<Card> cards) {
        // 检查是否已存在画像
        Optional<UserCognitiveProfile> existing = profileRepository.findByUserIdAndTopic(userId, topic);
        
        UserCognitiveProfile profile = existing.orElse(new UserCognitiveProfile());
        profile.setUserId(userId);
        profile.setTopic(topic);
        
        // 构建核心信念（使用AI分析）
        String coreBelief = generateCoreBelief(cards);
        profile.setCoreBelief(coreBelief);
        
        // 设置信念类型
        profile.setBeliefType(determineBeliefType(cards.size()));
        
        // 设置置信度
        BigDecimal confidence = calculateConfidence(cards);
        profile.setConfidenceLevel(confidence);
        
        // 设置证据数量
        profile.setEvidenceCount(cards.size());
        
        // 设置来源卡片ID
        String cardIds = cards.stream()
            .map(c -> String.valueOf(c.getId()))
            .reduce((a, b) -> a + "," + b)
            .orElse("");
        profile.setSourceCardIds(cardIds);
        
        // 设置关键词
        profile.setKeywords(extractKeywordsFromCards(cards));
        
        profile.setIsActive(true);
        
        profileRepository.save(profile);
    }

    /**
     * 生成核心信念
     */
    private String generateCoreBelief(List<Card> cards) {
        if (cards.isEmpty()) return "";
        
        // 使用第一张卡片的核心观点作为核心信念
        // 更复杂的实现可以使用AI总结
        return cards.get(0).getOneSentenceSummary();
    }

    /**
     * 确定信念类型
     */
    private UserCognitiveProfile.BeliefType determineBeliefType(int cardCount) {
        if (cardCount >= 5) return UserCognitiveProfile.BeliefType.STRONG_CONViction;
        if (cardCount >= 3) return UserCognitiveProfile.BeliefType.MODERATE_STANCE;
        if (cardCount >= 2) return UserCognitiveProfile.BeliefType.EXPLORING;
        return UserCognitiveProfile.BeliefType.TENTATIVE;
    }

    /**
     * 计算置信度
     */
    private BigDecimal calculateConfidence(List<Card> cards) {
        // 简单的置信度计算：卡片数量越多，置信度越高
        double confidence = Math.min(0.3 + cards.size() * 0.15, 0.95);
        return BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 使用AI分析冲突
     */
    private CognitiveConflictResult analyzeConflictWithAI(UserCognitiveProfile profile, Card card) {
        CognitiveConflictResult result = new CognitiveConflictResult();
        result.setProfile(profile);
        result.setCardViewpoint(card.getOneSentenceSummary());
        result.setUserBelief(profile.getCoreBelief());
        
        try {
            String prompt = buildConflictPrompt(profile, card);
            String aiResponse = callDeepSeekApi(prompt);
            
            // 解析AI响应
            parseConflictResponse(aiResponse, result);
            
            // 记录AI调用
            aiLogService.logAiCall(
                "COGNITIVE_CONFLICT_CHECK",
                prompt,
                aiResponse,
                result.isHasConflict() ? 1 : 0,
                null,
                0L
            );
            
        } catch (Exception e) {
            log.error("AI冲突分析失败", e);
            result.setHasConflict(false);
            result.setAiAnalysis("分析失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 构建冲突检测Prompt
     */
    private String buildConflictPrompt(UserCognitiveProfile profile, Card card) {
        return String.format("""
            请分析以下新认知卡片是否与用户已有的认知信念存在冲突。
            
            ## 用户的认知信念
            主题: %s
            核心信念: %s
            置信度: %s
            
            ## 新认知卡片
            标题: %s
            核心观点: %s
            摘要: %s
            关键词: %s
            
            ## 分析要求
            请判断这张新卡片的核心观点是否与用户的已有信念存在冲突、对立或矛盾。
            如果是补充、延伸或无关内容，不算冲突。
            
            请按以下JSON格式输出：
            {
                "hasConflict": true/false,
                "conflictType": "CONTRADICTORY|DIFFERENT_PERSPECTIVE|CHALLENGING|NONE",
                "conflictScore": 0.0-1.0,
                "description": "简要描述冲突内容",
                "analysis": "详细的AI分析说明"
            }
            
            只输出JSON，不要其他解释。
            """,
            profile.getTopic(),
            profile.getCoreBelief(),
            profile.getConfidenceLevel(),
            card.getTitle(),
            card.getOneSentenceSummary(),
            card.getKeyPoints(),
            card.getKeywords()
        );
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekApi(String prompt) {
        if (!deepSeekConfig.isEnabled()) {
            return simulateConflictAnalysis();
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

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("解析DeepSeek响应失败", e);
            return simulateConflictAnalysis();
        }
    }

    /**
     * 解析AI冲突响应
     */
    private void parseConflictResponse(String aiResponse, CognitiveConflictResult result) {
        try {
            String json = extractJson(aiResponse);
            JsonNode root = objectMapper.readTree(json);
            
            result.setHasConflict(root.path("hasConflict").asBoolean(false));
            result.setConflictType(root.path("conflictType").asText("NONE"));
            result.setConflictDescription(root.path("description").asText(""));
            result.setAiAnalysis(root.path("analysis").asText(""));
            result.setConflictScore(root.path("conflictScore").asDouble(0.0));
            
        } catch (Exception e) {
            log.error("解析冲突响应失败: {}", aiResponse, e);
            result.setHasConflict(false);
            result.setConflictScore(0.0);
        }
    }

    /**
     * 提取JSON
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
     * 模拟冲突分析
     */
    private String simulateConflictAnalysis() {
        boolean hasConflict = Math.random() > 0.8;
        
        if (hasConflict) {
            return """
                {
                    "hasConflict": true,
                    "conflictType": "CHALLENGING",
                    "conflictScore": 0.75,
                    "description": "新卡片对用户的现有信念提出了挑战",
                    "analysis": "新卡片的观点与用户的认知体系存在一定张力，建议用户仔细思考。"
                }
                """;
        } else {
            return """
                {
                    "hasConflict": false,
                    "conflictType": "NONE",
                    "conflictScore": 0.2,
                    "description": "",
                    "analysis": "新卡片与用户现有认知相容。"
                }
                """;
        }
    }

    /**
     * 从卡片提取主题
     */
    private String[] extractTopicsFromCard(Card card) {
        String keywords = card.getKeywords();
        if (keywords == null || keywords.isBlank()) {
            return new String[]{"通用"};
        }
        return keywords.split(",");
    }

    /**
     * 检查主题是否相关
     */
    private boolean isTopicRelated(String[] cardTopics, String profileTopic) {
        for (String topic : cardTopics) {
            BigDecimal similarity = textSimilarityService.calculateCosineSimilarity(
                topic.trim(), 
                profileTopic
            );
            if (similarity.doubleValue() >= TOPIC_MATCH_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新现有画像
     */
    private void updateExistingProfile(UserCognitiveProfile profile, Card card) {
        // 增加证据计数
        profile.setEvidenceCount(profile.getEvidenceCount() + 1);
        
        // 添加来源卡片ID
        String currentIds = profile.getSourceCardIds();
        profile.setSourceCardIds(currentIds + "," + card.getId());
        
        // 更新信念类型
        profile.setBeliefType(determineBeliefType(profile.getEvidenceCount()));
        
        // 更新置信度
        double newConfidence = Math.min(0.3 + profile.getEvidenceCount() * 0.15, 0.95);
        profile.setConfidenceLevel(BigDecimal.valueOf(newConfidence).setScale(2, RoundingMode.HALF_UP));
        
        profileRepository.save(profile);
    }

    /**
     * 创建新画像
     */
    private void createNewProfile(Long userId, String topic, Card card) {
        UserCognitiveProfile profile = new UserCognitiveProfile();
        profile.setUserId(userId);
        profile.setTopic(topic.trim());
        profile.setCoreBelief(card.getOneSentenceSummary());
        profile.setBeliefType(UserCognitiveProfile.BeliefType.TENTATIVE);
        profile.setConfidenceLevel(BigDecimal.valueOf(0.3));
        profile.setEvidenceCount(1);
        profile.setSourceCardIds(String.valueOf(card.getId()));
        profile.setKeywords(card.getKeywords());
        profile.setIsActive(true);
        
        profileRepository.save(profile);
    }

    /**
     * 从卡片提取关键词
     */
    private String extractKeywordsFromCards(List<Card> cards) {
        Set<String> keywords = new HashSet<>();
        for (Card card : cards) {
            if (card.getKeywords() != null) {
                Arrays.stream(card.getKeywords().split(","))
                    .map(String::trim)
                    .forEach(keywords::add);
            }
        }
        return String.join(",", keywords);
    }
}
