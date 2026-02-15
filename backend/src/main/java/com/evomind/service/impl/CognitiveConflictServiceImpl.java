package com.evomind.service.impl;

import com.evomind.entity.Card;
import com.evomind.entity.CognitiveConflict;
import com.evomind.entity.UserCognitiveProfile;
import com.evomind.exception.BusinessException;
import com.evomind.repository.CardRepository;
import com.evomind.repository.CognitiveConflictRepository;
import com.evomind.service.CardService;
import com.evomind.service.CognitiveConflictService;
import com.evomind.service.CognitiveProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 认知冲突服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CognitiveConflictServiceImpl implements CognitiveConflictService {

    private final CognitiveConflictRepository conflictRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final CognitiveProfileService profileService;

    @Override
    @Transactional
    public List<CognitiveConflict> detectConflicts(Long userId, Card card) {
        log.info("检测卡片与用户认知体系的冲突: userId={}, cardId={}", userId, card.getId());

        // 使用CognitiveProfileService检查冲突
        List<CognitiveProfileService.CognitiveConflictResult> results = 
            profileService.checkConflictWithProfile(userId, card);

        // 转换为实体并保存
        List<CognitiveConflict> savedConflicts = new ArrayList<>();
        
        for (CognitiveProfileService.CognitiveConflictResult result : results) {
            if (result.isHasConflict()) {
                UserCognitiveProfile profile = result.getProfile();
                
                // 检查是否已存在
                if (conflictRepository.existsByCardIdAndProfileIdAndUserId(
                        card.getId(), profile.getId(), userId)) {
                    continue;
                }
                
                CognitiveConflict conflict = new CognitiveConflict();
                conflict.setUserId(userId);
                conflict.setCardId(card.getId());
                conflict.setProfileId(profile.getId());
                conflict.setConflictType(result.getConflictType());
                conflict.setConflictDescription(result.getConflictDescription());
                conflict.setUserBelief(result.getUserBelief());
                conflict.setCardViewpoint(result.getCardViewpoint());
                conflict.setConflictScore(BigDecimal.valueOf(result.getConflictScore())
                    .setScale(4, RoundingMode.HALF_UP));
                conflict.setAiAnalysis(result.getAiAnalysis());
                conflict.setTopic(profile.getTopic());
                conflict.setIsAcknowledged(false);
                conflict.setIsDismissed(false);
                
                savedConflicts.add(conflictRepository.save(conflict));
                
                log.info("保存认知冲突: cardId={}, profileId={}, type={}", 
                    card.getId(), profile.getId(), result.getConflictType());
            }
        }

        // 更新卡片的冲突标志
        if (!savedConflicts.isEmpty()) {
            card.setHasConflict(true);
            cardRepository.save(card);
        }

        return savedConflicts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CognitiveConflict> getUnresolvedConflicts(Long userId) {
        return conflictRepository.findByUserIdAndIsAcknowledgedFalseAndIsDismissedFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CognitiveConflict> getConflictsByCard(Long cardId, Long userId) {
        return conflictRepository.findByCardIdAndUserId(cardId, userId);
    }

    @Override
    @Transactional
    public void acknowledgeConflict(Long conflictId, Long userId) {
        CognitiveConflict conflict = conflictRepository.findById(conflictId)
            .orElseThrow(() -> new BusinessException("冲突记录不存在"));
        
        if (!conflict.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此冲突记录");
        }
        
        conflict.setIsAcknowledged(true);
        conflictRepository.save(conflict);
        
        log.info("用户确认认知冲突: conflictId={}, userId={}", conflictId, userId);
    }

    @Override
    @Transactional
    public void dismissConflict(Long conflictId, Long userId) {
        CognitiveConflict conflict = conflictRepository.findById(conflictId)
            .orElseThrow(() -> new BusinessException("冲突记录不存在"));
        
        if (!conflict.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此冲突记录");
        }
        
        conflict.setIsDismissed(true);
        conflictRepository.save(conflict);
        
        log.info("用户忽略认知冲突: conflictId={}, userId={}", conflictId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnresolvedConflictCount(Long userId) {
        return conflictRepository.countByUserIdAndIsAcknowledgedFalseAndIsDismissedFalse(userId);
    }

    @Override
    @Async("taskExecutor")
    public void autoDetectOnCardSave(Long userId, Long cardId) {
        try {
            log.info("自动检测认知冲突: userId={}, cardId={}", userId, cardId);
            
            Card card = cardService.getCardById(cardId, userId);
            
            // 检测冲突
            List<CognitiveConflict> conflicts = detectConflicts(userId, card);
            
            // 更新用户认知画像
            profileService.updateProfileWithCard(userId, card);
            
            log.info("自动检测完成: 发现 {} 个冲突", conflicts.size());
        } catch (Exception e) {
            log.error("自动检测认知冲突失败: userId={}, cardId={}", userId, cardId, e);
        }
    }
}
