package com.evomind.service.impl;

import com.evomind.entity.Card;
import com.evomind.entity.CardConflict;
import com.evomind.exception.BusinessException;
import com.evomind.repository.CardConflictRepository;
import com.evomind.repository.CardRepository;
import com.evomind.service.CardService;
import com.evomind.service.ConflictDetectionService;
import com.evomind.service.OpinionAnalysisService;
import com.evomind.service.TextSimilarityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConflictDetectionServiceImpl implements ConflictDetectionService {

    private final CardConflictRepository conflictRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final TextSimilarityService textSimilarityService;
    private final OpinionAnalysisService opinionAnalysisService;

    // 相似度阈值
    private static final double SIMILARITY_THRESHOLD = 0.5;
    private static final double CONFLICT_SCORE_THRESHOLD = 0.6;

    @Override
    @Transactional
    public List<CardConflict> detectConflicts(Long cardId) {
        Card card = cardService.getCardById(cardId, null);
        Long userId = card.getUserId();
        
        log.info("开始检测卡片冲突: cardId={}, userId={}", cardId, userId);
        
        // 获取用户所有卡片（按时间倒序，限制最近100张以提高性能）
        List<Card> userCards = cardRepository.findTop100ByUserIdOrderByCreatedAtDesc(userId);
        
        // 阶段1: 快速过滤 - 使用文本相似度找出主题相关的卡片
        List<Card> candidateCards = filterBySimilarity(card, userCards);
        log.info("相似度过滤后候选卡片数: {}", candidateCards.size());
        
        // 阶段2: AI深度分析 - 使用DeepSeek API分析观点冲突
        List<OpinionAnalysisService.OpinionConflictResult> aiResults =
            opinionAnalysisService.analyzeBatchConflicts(card, candidateCards);
        
        // 阶段3: 保存检测到的冲突
        List<CardConflict> detectedConflicts = new ArrayList<>();
        
        for (OpinionAnalysisService.OpinionConflictResult result : aiResults) {
            if (result.isHasConflict() && result.getConflictScore().doubleValue() >= CONFLICT_SCORE_THRESHOLD) {
                CardConflict conflict = saveConflictResult(result, userId);
                detectedConflicts.add(conflict);
                
                // 更新卡片的冲突标志
                updateCardConflictFlag(result.getCard1().getId(), userId);
                updateCardConflictFlag(result.getCard2().getId(), userId);
            }
        }
        
        log.info("检测到 {} 个冲突", detectedConflicts.size());
        return detectedConflicts;
    }
    
    /**
     * 基于文本相似度过滤候选卡片
     */
    private List<Card> filterBySimilarity(Card targetCard, List<Card> allCards) {
        List<Card> candidates = new ArrayList<>();
        
        for (Card otherCard : allCards) {
            if (otherCard.getId().equals(targetCard.getId())) {
                continue;
            }
            
            // 检查是否已存在冲突记录
            if (conflictRepository.existsByCardId1AndCardId2AndUserId(
                    Math.min(targetCard.getId(), otherCard.getId()),
                    Math.max(targetCard.getId(), otherCard.getId()),
                    targetCard.getUserId())) {
                continue;
            }
            
            // 计算关键词Jaccard相似度
            BigDecimal keywordSimilarity = textSimilarityService.calculateJaccardSimilarity(
                targetCard.getKeywords(),
                otherCard.getKeywords()
            );
            
            // 计算内容余弦相似度（使用标题+摘要+核心观点）
            String content1 = buildComparisonContent(targetCard);
            String content2 = buildComparisonContent(otherCard);
            BigDecimal contentSimilarity = textSimilarityService.calculateCosineSimilarity(content1, content2);
            
            // 综合相似度
            double combinedScore = keywordSimilarity.doubleValue() * 0.6 + contentSimilarity.doubleValue() * 0.4;
            
            if (combinedScore >= SIMILARITY_THRESHOLD) {
                candidates.add(otherCard);
            }
        }
        
        // 限制候选数量，优先选择相似度高的
        return candidates.stream()
            .limit(10)
            .toList();
    }
    
    /**
     * 构建用于对比的文本内容
     */
    private String buildComparisonContent(Card card) {
        StringBuilder sb = new StringBuilder();
        if (card.getTitle() != null) sb.append(card.getTitle()).append(" ");
        if (card.getOneSentenceSummary() != null) sb.append(card.getOneSentenceSummary()).append(" ");
        if (card.getKeyPoints() != null) sb.append(card.getKeyPoints());
        return sb.toString();
    }
    
    /**
     * 保存冲突分析结果
     */
    private CardConflict saveConflictResult(OpinionAnalysisService.OpinionConflictResult result, Long userId) {
        CardConflict conflict = new CardConflict();
        conflict.setCardId1(Math.min(result.getCard1().getId(), result.getCard2().getId()));
        conflict.setCardId2(Math.max(result.getCard1().getId(), result.getCard2().getId()));
        conflict.setUserId(userId);
        conflict.setConflictType(result.getConflictType());
        conflict.setConflictDescription(result.getConflictDescription());
        conflict.setTopic(result.getTopic());
        conflict.setConflictScore(result.getConflictScore());
        conflict.setAiAnalysis(result.getAiAnalysis());
        conflict.setIsAcknowledged(false);
        
        // 计算综合相似度分数
        BigDecimal similarityScore = textSimilarityService.calculateCosineSimilarity(
            buildComparisonContent(result.getCard1()),
            buildComparisonContent(result.getCard2())
        );
        conflict.setSimilarityScore(similarityScore);
        
        return conflictRepository.save(conflict);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardConflict> getUnresolvedConflicts(Long userId) {
        return conflictRepository.findByUserIdAndIsAcknowledgedFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardConflict> getConflictsByCard(Long cardId, Long userId) {
        return conflictRepository.findByCardIdAndUserId(cardId, userId);
    }

    @Override
    @Transactional
    public void acknowledgeConflict(Long conflictId, Long userId) {
        CardConflict conflict = conflictRepository.findById(conflictId)
                .orElseThrow(() -> new BusinessException("冲突记录不存在"));
        
        if (!conflict.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此冲突记录");
        }
        
        conflict.setIsAcknowledged(true);
        conflictRepository.save(conflict);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConflictBetween(Long cardId1, Long cardId2, Long userId) {
        return conflictRepository.findConflictBetweenCards(cardId1, cardId2, userId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnresolvedConflictCount(Long userId) {
        return conflictRepository.countByUserIdAndIsAcknowledgedFalse(userId);
    }

    /**
     * 异步触发冲突检测（在卡片创建后自动执行）
     */
    @Async("taskExecutor")
    public void triggerAsyncConflictDetection(Long cardId) {
        try {
            log.info("异步触发冲突检测: cardId={}", cardId);
            detectConflicts(cardId);
        } catch (Exception e) {
            log.error("异步冲突检测失败: cardId={}", cardId, e);
        }
    }

    private void updateCardConflictFlag(Long cardId, Long userId) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId).orElse(null);
        if (card != null && !Boolean.TRUE.equals(card.getHasConflict())) {
            card.setHasConflict(true);
            cardRepository.save(card);
        }
    }

    private String extractCommonTopic(String title1, String title2) {
        // 简单提取共同主题词
        if (title1 == null || title2 == null) {
            return "通用主题";
        }
        // TODO: 使用NLP提取主题
        return title1.length() > 10 ? title1.substring(0, 10) + "..." : title1;
    }
}
