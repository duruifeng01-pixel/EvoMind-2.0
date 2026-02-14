package com.evomind.service.impl;

import com.evomind.entity.Card;
import com.evomind.entity.CardConflict;
import com.evomind.exception.BusinessException;
import com.evomind.repository.CardConflictRepository;
import com.evomind.repository.CardRepository;
import com.evomind.service.CardService;
import com.evomind.service.ConflictDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConflictDetectionServiceImpl implements ConflictDetectionService {

    private final CardConflictRepository conflictRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;

    @Override
    @Transactional
    public List<CardConflict> detectConflicts(Long cardId) {
        Card card = cardService.getCardById(cardId, null);
        Long userId = card.getUserId();
        
        List<Card> userCards = cardRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<CardConflict> detectedConflicts = new ArrayList<>();
        
        for (Card otherCard : userCards) {
            if (otherCard.getId().equals(cardId)) {
                continue;
            }
            
            // 检查是否已存在冲突记录
            if (conflictRepository.existsByCardId1AndCardId2AndUserId(
                    Math.min(cardId, otherCard.getId()), 
                    Math.max(cardId, otherCard.getId()), 
                    userId)) {
                continue;
            }
            
            // TODO: 调用AI服务进行冲突检测
            // 这里使用简单的关键词匹配作为示例
            CardConflict conflict = checkConflictWithAI(card, otherCard);
            
            if (conflict != null) {
                conflict.setUserId(userId);
                conflictRepository.save(conflict);
                detectedConflicts.add(conflict);
                
                // 更新卡片的冲突标志
                updateCardConflictFlag(cardId, userId);
                updateCardConflictFlag(otherCard.getId(), userId);
            }
        }
        
        return detectedConflicts;
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

    private CardConflict checkConflictWithAI(Card card1, Card card2) {
        // TODO: 集成AI服务进行深度冲突检测
        // 目前使用简单的关键词匹配作为占位实现
        
        String keywords1 = card1.getKeywords();
        String keywords2 = card2.getKeywords();
        
        if (keywords1 == null || keywords2 == null) {
            return null;
        }
        
        // 简单的关键词重叠检测
        String[] keys1 = keywords1.split(",");
        String[] keys2 = keywords2.split(",");
        
        int overlapCount = 0;
        for (String k1 : keys1) {
            for (String k2 : keys2) {
                if (k1.trim().equalsIgnoreCase(k2.trim())) {
                    overlapCount++;
                }
            }
        }
        
        // 如果有足够的关键词重叠，认为可能存在冲突
        if (overlapCount >= 2) {
            CardConflict conflict = new CardConflict();
            conflict.setCardId1(Math.min(card1.getId(), card2.getId()));
            conflict.setCardId2(Math.max(card1.getId(), card2.getId()));
            conflict.setConflictType("TOPIC_OVERLAP");
            conflict.setConflictDescription("检测到主题重叠，可能存在观点关联或冲突");
            conflict.setTopic(extractCommonTopic(card1.getTitle(), card2.getTitle()));
            conflict.setSimilarityScore(BigDecimal.valueOf(0.6));
            conflict.setConflictScore(BigDecimal.valueOf(0.3));
            conflict.setAiAnalysis("AI检测到两张卡片在主题上存在关联，建议进一步查看是否有观点冲突。");
            return conflict;
        }
        
        return null;
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
