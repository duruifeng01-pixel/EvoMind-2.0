package com.evomind.service.impl;

import com.evomind.dto.response.CardResponse;
import com.evomind.entity.Card;
import com.evomind.entity.UserReadingHistory;
import com.evomind.repository.CardRepository;
import com.evomind.repository.UserReadingHistoryRepository;
import com.evomind.service.FeedService;
import com.evomind.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Feed流服务实现
 * 实现7:3智能混合信息流
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final CardRepository cardRepository;
    private final UserReadingHistoryRepository readingHistoryRepository;
    private final RecommendationService recommendationService;

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

        // 1. 获取70%自选源内容
        List<CardResponse> userSourceContent = getUserSourceContent(userId, userSourceCount);
        Set<Long> existingIds = userSourceContent.stream()
                .map(CardResponse::getId)
                .collect(Collectors.toSet());

        // 2. 获取30%系统推荐内容（排除已在70%中的）
        List<Long> excludeIds = new ArrayList<>(existingIds);
        List<Card> recommendedCards = recommendationService.recommendCards(
                userId, recommendCount, excludeIds);

        List<CardResponse> recommendedContent = recommendedCards.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // 3. 智能混合（使用洗牌算法，但保持大致比例）
        List<CardResponse> mixedFeed = smartShuffle(userSourceContent, recommendedContent);

        // 4. 如果不足，补充更多内容
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
        // 获取用户关注的信息源的最新卡片
        List<Card> cards = cardRepository.findByUserSourcesOrderByCreatedAtDesc(
                userId, PageRequest.of(0, limit));

        return cards.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardResponse> getSystemRecommendations(Long userId, int limit) {
        List<Long> excludeIds = readingHistoryRepository.findReadCardIdsByUserId(userId);
        List<Card> recommended = recommendationService.recommendCards(userId, limit, excludeIds);

        return recommended.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
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
        // 重新构建用户兴趣画像
        recommendationService.buildUserInterestProfile(userId);
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
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setTitle(card.getTitle());
        response.setSummaryText(card.getSummaryText());
        response.setOneSentenceSummary(card.getOneSentenceSummary());
        response.setKeywords(card.getKeywords());
        response.setReadingTimeMinutes(card.getReadingTimeMinutes());
        response.setSourceTitle(card.getSourceTitle());
        response.setCreatedAt(card.getCreatedAt());
        response.setViewCount(card.getViewCount());
        response.setHasConflict(card.getHasConflict());
        return response;
    }
}
