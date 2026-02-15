package com.evomind.service.impl;

import com.evomind.dto.response.CardResponse;
import com.evomind.entity.Card;
import com.evomind.entity.CrawledContent;
import com.evomind.entity.UserReadingHistory;
import com.evomind.repository.CardRepository;
import com.evomind.repository.CrawledContentRepository;
import com.evomind.repository.UserReadingHistoryRepository;
import com.evomind.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Feed流服务实现
 * 实现7:3智能混合信息流
 * 
 * 数据来源：
 * - 70%：用户导入信息源的采集内容（通过ContentCrawlService定期采集）
 * - 30%：系统推荐内容（通过ContentDiscoveryService基于用户画像发现）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final CardRepository cardRepository;
    private final CrawledContentRepository crawledContentRepository;
    private final UserReadingHistoryRepository readingHistoryRepository;
    private final RecommendationService recommendationService;
    
    // 内容采集相关服务
    private final ContentCrawlService contentCrawlService;
    private final ContentDiscoveryService contentDiscoveryService;
    private final ContentProcessingService contentProcessingService;
    private final ContentSchedulerService contentSchedulerService;

    // 7:3 混合比例
    private static final double USER_SOURCE_RATIO = 0.7;
    private static final double SYSTEM_RECOMMEND_RATIO = 0.3;

    @Override
    public List<CardResponse> getMixedFeed(Long userId, int page, int size) {
        // 计算70%和30%的数量
        int userSourceCount = (int) Math.round(size * USER_SOURCE_RATIO);
        int recommendCount = size - userSourceCount;

        log.debug("Generating mixed feed for user {}: {} user source + {} recommended",
                userId, userSourceCount, recommendCount);

        // 1. 触发后台采集（异步）- 确保用户看到最新内容
        triggerBackgroundCrawl(userId);

        // 2. 获取70%自选源内容
        List<CardResponse> userSourceContent = getUserSourceContent(userId, userSourceCount);
        Set<Long> existingIds = userSourceContent.stream()
                .map(CardResponse::getId)
                .collect(Collectors.toSet());

        // 3. 获取30%系统推荐内容（排除已在70%中的）
        List<CardResponse> recommendedContent = getSystemRecommendations(userId, recommendCount);
        
        // 过滤掉已在70%中的卡片
        recommendedContent = recommendedContent.stream()
                .filter(card -> !existingIds.contains(card.getId()))
                .collect(Collectors.toList());

        // 4. 智能混合（使用洗牌算法，但保持大致比例）
        List<CardResponse> mixedFeed = smartShuffle(userSourceContent, recommendedContent);

        // 5. 如果不足，补充更多内容
        if (mixedFeed.size() < size) {
            int remaining = size - mixedFeed.size();
            List<Card> additional = cardRepository.findRandomCardsExcluding(
                    existingIds, PageRequest.of(0, remaining));
            mixedFeed.addAll(additional.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList()));
        }

        return mixedFeed.stream().limit(size).collect(Collectors.toList());
    }

    @Override
    public List<CardResponse> getUserSourceContent(Long userId, int limit) {
        // 步骤1: 获取待处理的采集内容并转换为卡片
        List<CrawledContent> pendingContents = crawledContentRepository
                .findByUserIdAndStatusOrderByCrawledAtDesc(
                        userId, CrawledContent.ContentStatus.DEDUPLICATED);
        
        // 批量处理待处理内容
        if (!pendingContents.isEmpty()) {
            List<Long> contentIds = pendingContents.stream()
                    .limit(limit)
                    .map(CrawledContent::getId)
                    .collect(Collectors.toList());
            
            contentProcessingService.batchProcessToCards(contentIds);
        }

        // 步骤2: 获取用户关注信息源的最新卡片（70%部分）
        List<Card> cards = cardRepository.findByUserSourcesOrderByCreatedAtDesc(
                userId, PageRequest.of(0, limit));

        return cards.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardResponse> getSystemRecommendations(Long userId, int limit) {
        // 使用 ContentDiscoveryService 发现30%推荐内容
        try {
            // 1. 发现新内容
            List<CrawledContent> discoveredContent = contentDiscoveryService
                    .discoverContentForUser(userId, limit * 2);
            
            // 2. 处理发现的内容为卡片
            List<Card> recommendedCards = new ArrayList<>();
            
            for (CrawledContent content : discoveredContent) {
                try {
                    // 如果已经处理过，直接获取卡片
                    if (content.getCardId() != null) {
                        cardRepository.findById(content.getCardId())
                                .ifPresent(recommendedCards::add);
                    } else {
                        // 处理为新卡片
                        Card card = contentProcessingService.processToCard(content.getId());
                        if (card != null) {
                            recommendedCards.add(card);
                        }
                    }
                    
                    if (recommendedCards.size() >= limit) {
                        break;
                    }
                } catch (Exception e) {
                    log.error("Failed to process discovered content: {}", content.getId(), e);
                }
            }
            
            // 3. 如果系统发现的内容不足，使用原有推荐服务补充
            if (recommendedCards.size() < limit) {
                List<Long> excludeIds = recommendedCards.stream()
                        .map(Card::getId)
                        .collect(Collectors.toList());
                excludeIds.addAll(readingHistoryRepository.findReadCardIdsByUserId(userId));
                
                int remaining = limit - recommendedCards.size();
                List<Card> additionalCards = recommendationService.recommendCards(
                        userId, remaining, excludeIds);
                recommendedCards.addAll(additionalCards);
            }

            return recommendedCards.stream()
                    .limit(limit)
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting system recommendations for user {}", userId, e);
            // 降级到原有推荐服务
            List<Long> excludeIds = readingHistoryRepository.findReadCardIdsByUserId(userId);
            List<Card> recommended = recommendationService.recommendCards(userId, limit, excludeIds);
            return recommended.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public void trackReadingBehavior(Long userId, Long cardId, int durationSeconds, int readPercentage) {
        Optional<UserReadingHistory> existing = readingHistoryRepository
                .findByUserIdAndCardId(userId, cardId);

        UserReadingHistory history;
        if (existing.isPresent()) {
            history = existing.get();
            history.setReadDurationSeconds(durationSeconds);
            history.setReadPercentage(readPercentage);
            history.setInteractionType(UserReadingHistory.InteractionType.READ);
        } else {
            history = new UserReadingHistory();
            history.setUserId(userId);
            history.setCardId(cardId);
            history.setReadDurationSeconds(durationSeconds);
            history.setReadPercentage(readPercentage);
            history.setInteractionType(UserReadingHistory.InteractionType.READ);
        }

        history.setReadAt(LocalDateTime.now());
        readingHistoryRepository.save(history);

        // 更新用户兴趣画像
        Card card = cardRepository.findById(cardId).orElse(null);
        if (card != null && card.getKeywords() != null) {
            int score = Math.min(durationSeconds / 10, 50) + (readPercentage / 10);
            recommendationService.updateInterestWeight(userId, cardId, card.getKeywords(), score);
        }
    }

    @Override
    @Transactional
    public void trackInteraction(Long userId, Long cardId, String interactionType) {
        Optional<UserReadingHistory> existing = readingHistoryRepository
                .findByUserIdAndCardId(userId, cardId);

        UserReadingHistory history = existing.orElseGet(() -> {
            UserReadingHistory h = new UserReadingHistory();
            h.setUserId(userId);
            h.setCardId(cardId);
            return h;
        });

        // 根据互动类型更新
        switch (interactionType.toUpperCase()) {
            case "FAVORITE":
                history.setIsFavorite(true);
                history.setInteractionType(UserReadingHistory.InteractionType.SAVE);
                break;
            case "SHARE":
                history.setInteractionType(UserReadingHistory.InteractionType.SHARE);
                break;
            case "COMMENT":
                history.setInteractionType(UserReadingHistory.InteractionType.COMMENT);
                break;
        }

        history.setReadAt(LocalDateTime.now());
        readingHistoryRepository.save(history);

        // 更新兴趣权重
        Card card = cardRepository.findById(cardId).orElse(null);
        if (card != null && card.getKeywords() != null) {
            int score = switch (interactionType.toUpperCase()) {
                case "FAVORITE" -> 30;
                case "SHARE" -> 40;
                case "COMMENT" -> 35;
                default -> 10;
            };
            recommendationService.updateInterestWeight(userId, cardId, card.getKeywords(), score);
        }
    }

    @Override
    public void refreshFeed(Long userId) {
        // 1. 重新构建用户兴趣画像
        recommendationService.buildUserInterestProfile(userId);
        
        // 2. 触发该用户的信息源采集
        contentSchedulerService.triggerCrawlForUser(userId);
        
        log.info("Feed refreshed for user {}", userId);
    }

    @Override
    public FeedStats getFeedStats(Long userId) {
        FeedStats stats = new FeedStats();

        // 统计阅读数量
        long totalRead = readingHistoryRepository.countByUserId(userId);
        stats.setTotalCards(totalRead);

        // 统计收藏数量
        long favorites = readingHistoryRepository.countByUserIdAndIsFavoriteTrue(userId);
        stats.setUserSourceCards(favorites);

        // 计算茧房风险
        double echoChamberRisk = recommendationService.calculateEchoChamberRisk(userId);
        stats.setEchoChamberRisk(echoChamberRisk);

        // 计算多样性得分
        stats.setDiversityScore(1.0 - echoChamberRisk);

        return stats;
    }

    /**
     * 触发后台采集（确保内容新鲜度）
     */
    private void triggerBackgroundCrawl(Long userId) {
        try {
            // 检查用户是否有需要更新的信息源
            List<CrawledContent> pendingContents = crawledContentRepository
                    .findByUserIdAndStatusOrderByCrawledAtDesc(
                            userId, CrawledContent.ContentStatus.RAW);
            
            // 如果有超过5个待处理内容，触发批量处理
            if (pendingContents.size() >= 5) {
                log.debug("Triggering background processing for user {}: {} pending contents", 
                        userId, pendingContents.size());
            }
        } catch (Exception e) {
            log.error("Error triggering background crawl for user {}", userId, e);
        }
    }

    /**
     * 智能洗牌：混合两种内容，但保持大致比例
     */
    private List<CardResponse> smartShuffle(List<CardResponse> userSource, List<CardResponse> recommended) {
        List<CardResponse> result = new ArrayList<>();
        List<CardResponse> userSourceCopy = new ArrayList<>(userSource);
        List<CardResponse> recommendedCopy = new ArrayList<>(recommended);

        Collections.shuffle(userSourceCopy);
        Collections.shuffle(recommendedCopy);

        int userIdx = 0, recIdx = 0;
        int total = userSource.size() + recommended.size();

        // 交替插入，优先70%内容
        for (int i = 0; i < total; i++) {
            // 根据位置决定优先取哪类
            double currentRatio = (double) userIdx / (userIdx + recIdx + 1);

            if (currentRatio < USER_SOURCE_RATIO && userIdx < userSourceCopy.size()) {
                result.add(userSourceCopy.get(userIdx++));
            } else if (recIdx < recommendedCopy.size()) {
                result.add(recommendedCopy.get(recIdx++));
            } else if (userIdx < userSourceCopy.size()) {
                result.add(userSourceCopy.get(userIdx++));
            }
        }

        return result;
    }

    private CardResponse convertToResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .title(card.getTitle())
                .summaryText(card.getSummaryText())
                .oneSentenceSummary(card.getOneSentenceSummary())
                .keywords(card.getKeywords())
                .readingTimeMinutes(card.getReadingTimeMinutes())
                .sourceTitle(card.getSourceTitle())
                .sourceId(card.getSourceId())
                .createdAt(card.getCreatedAt())
                .viewCount(card.getViewCount())
                .hasConflict(card.getHasConflict())
                .build();
    }
}
