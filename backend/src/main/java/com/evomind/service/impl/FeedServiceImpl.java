package com.evomind.service.impl;

import com.evomind.dto.response.CardResponse;
import com.evomind.entity.Card;
import com.evomind.entity.CrawledContent;
import com.evomind.entity.UserDailyFeedQuota;
import com.evomind.entity.UserReadCardRecord;
import com.evomind.entity.UserReadingHistory;
import com.evomind.repository.*;
import com.evomind.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Feed流服务实现
 * 实现7:3智能混合信息流
 *
 * 控制机制：
 * - 每日配额：300条/天
 * - 已读防重复：3天冷却期
 * - 自动归档：7天未读内容自动归档
 *
 * 数据来源：
 * - 70%：用户导入信息源的采集内容
 * - 30%：系统推荐内容
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final CardRepository cardRepository;
    private final CrawledContentRepository crawledContentRepository;
    private final UserReadingHistoryRepository readingHistoryRepository;
    private final UserDailyFeedQuotaRepository quotaRepository;
    private final UserReadCardRecordRepository readCardRecordRepository;
    private final RecommendationService recommendationService;

    // 内容采集相关服务
    private final ContentCrawlService contentCrawlService;
    private final ContentDiscoveryService contentDiscoveryService;
    private final ContentProcessingService contentProcessingService;
    private final ContentSchedulerService contentSchedulerService;

    // 7:3 混合比例
    private static final double USER_SOURCE_RATIO = 0.7;
    private static final double SYSTEM_RECOMMEND_RATIO = 0.3;

    // 控制参数
    private static final int DAILY_LIMIT = 300;          // 每日限额
    private static final int COOL_DOWN_DAYS = 3;         // 已读内容冷却期（天）
    private static final int ARCHIVE_AFTER_DAYS = 7;     // 自动归档期限（天）

    @Override
    public List<CardResponse> getMixedFeed(Long userId, int page, int size) {
        // 1. 检查每日配额
        UserDailyFeedQuota quota = getOrCreateQuota(userId);
        if (quota.getIsExhausted()) {
            log.info("User {} has exhausted daily quota ({}/{})",
                    userId, quota.getConsumedCount(), quota.getDailyLimit());
            return Collections.emptyList();
        }

        // 限制请求数量不超过剩余配额
        int remainingQuota = quota.getRemainingCount();
        int actualSize = Math.min(size, remainingQuota);

        if (actualSize <= 0) {
            return Collections.emptyList();
        }

        // 2. 计算70%和30%的数量
        int userSourceCount = (int) Math.round(actualSize * USER_SOURCE_RATIO);
        int recommendCount = actualSize - userSourceCount;

        log.debug("Generating mixed feed for user {}: {} user source + {} recommended (quota: {}/{})",
                userId, userSourceCount, recommendCount, quota.getConsumedCount(), quota.getDailyLimit());

        // 3. 触发后台采集
        triggerBackgroundCrawl(userId);

        // 4. 获取已读内容（冷却期内不重复推荐）
        Set<Long> excludedCardIds = getExcludedCardIds(userId);

        // 5. 获取70%自选源内容
        List<CardResponse> userSourceContent = getUserSourceContent(userId, userSourceCount, excludedCardIds);
        Set<Long> existingIds = userSourceContent.stream()
                .map(CardResponse::getId)
                .collect(Collectors.toSet());
        excludedCardIds.addAll(existingIds);

        // 6. 获取30%系统推荐内容
        List<CardResponse> recommendedContent = getSystemRecommendations(userId, recommendCount, excludedCardIds);

        // 7. 智能混合
        List<CardResponse> mixedFeed = smartShuffle(userSourceContent, recommendedContent);

        // 8. 如果不足，尝试补充（不违反配额）
        if (mixedFeed.size() < actualSize && quota.getRemainingCount() > mixedFeed.size()) {
            int remaining = Math.min(actualSize - mixedFeed.size(), quota.getRemainingCount() - mixedFeed.size());
            List<Card> additional = cardRepository.findRandomCardsExcluding(
                    excludedCardIds, PageRequest.of(0, remaining));
            mixedFeed.addAll(additional.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList()));
        }

        // 9. 消费配额
        int consumed = mixedFeed.size();
        if (consumed > 0) {
            quota.consume(consumed, false); // 混合消费，稍后按实际比例调整
            quotaRepository.save(quota);
        }

        return mixedFeed.stream().limit(actualSize).collect(Collectors.toList());
    }

    @Override
    public List<CardResponse> getUserSourceContent(Long userId, int limit, Set<Long> excludeIds) {
        // 步骤1: 处理待采集内容
        List<CrawledContent> pendingContents = crawledContentRepository
                .findByUserIdAndStatusOrderByCrawledAtDesc(
                        userId, CrawledContent.ContentStatus.DEDUPLICATED);

        if (!pendingContents.isEmpty()) {
            List<Long> contentIds = pendingContents.stream()
                    .limit(limit)
                    .map(CrawledContent::getId)
                    .collect(Collectors.toList());
            contentProcessingService.batchProcessToCards(contentIds);
        }

        // 步骤2: 获取用户关注信息源的最新卡片（排除已读和指定ID）
        List<Card> cards = cardRepository.findByUserSourcesOrderByCreatedAtDesc(
                userId, PageRequest.of(0, limit * 2)); // 多获取一些以应对过滤

        return cards.stream()
                .filter(card -> !excludeIds.contains(card.getId()))
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardResponse> getSystemRecommendations(Long userId, int limit, Set<Long> excludeIds) {
        try {
            // 使用 ContentDiscoveryService 发现30%推荐内容
            List<CrawledContent> discoveredContent = contentDiscoveryService
                    .discoverContentForUser(userId, limit * 2);

            List<Card> recommendedCards = new ArrayList<>();

            for (CrawledContent content : discoveredContent) {
                try {
                    if (content.getCardId() != null && !excludeIds.contains(content.getCardId())) {
                        cardRepository.findById(content.getCardId())
                                .ifPresent(recommendedCards::add);
                    } else if (content.getCardId() == null) {
                        Card card = contentProcessingService.processToCard(content.getId());
                        if (card != null && !excludeIds.contains(card.getId())) {
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

            // 如果系统发现的内容不足，使用原有推荐服务补充
            if (recommendedCards.size() < limit) {
                List<Long> additionalExcludeIds = new ArrayList<>(excludeIds);
                additionalExcludeIds.addAll(recommendedCards.stream()
                        .map(Card::getId).toList());

                int remaining = limit - recommendedCards.size();
                List<Card> additionalCards = recommendationService.recommendCards(
                        userId, remaining, additionalExcludeIds);
                recommendedCards.addAll(additionalCards);
            }

            return recommendedCards.stream()
                    .limit(limit)
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting system recommendations for user {}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public void trackReadingBehavior(Long userId, Long cardId, int durationSeconds, int readPercentage) {
        // 记录阅读历史
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

        // 记录已读防重复
        recordCardRead(userId, cardId);

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

        // 记录已读
        recordCardRead(userId, cardId);

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
        recommendationService.buildUserInterestProfile(userId);
        contentSchedulerService.triggerCrawlForUser(userId);
        log.info("Feed refreshed for user {}", userId);
    }

    @Override
    public FeedStats getFeedStats(Long userId) {
        FeedStats stats = new FeedStats();

        // 获取今日配额信息
        UserDailyFeedQuota quota = getOrCreateQuota(userId);
        stats.setDailyLimit(quota.getDailyLimit());
        stats.setConsumedToday(quota.getConsumedCount());
        stats.setRemainingToday(quota.getRemainingCount());
        stats.setIsExhausted(quota.getIsExhausted());

        // 其他统计
        long totalRead = readingHistoryRepository.countByUserId(userId);
        stats.setTotalCards(totalRead);

        long favorites = readingHistoryRepository.countByUserIdAndIsFavoriteTrue(userId);
        stats.setUserSourceCards(favorites);

        double echoChamberRisk = recommendationService.calculateEchoChamberRisk(userId);
        stats.setEchoChamberRisk(echoChamberRisk);
        stats.setDiversityScore(1.0 - echoChamberRisk);

        return stats;
    }

    // ============ 私有方法 ============

    /**
     * 获取或创建用户每日配额
     */
    private UserDailyFeedQuota getOrCreateQuota(Long userId) {
        LocalDate today = LocalDate.now();
        Optional<UserDailyFeedQuota> existing = quotaRepository
                .findByUserIdAndQuotaDate(userId, today);

        if (existing.isPresent()) {
            UserDailyFeedQuota quota = existing.get();
            // 检查是否需要重置（新的一天）
            if (quota.needsReset()) {
                quota.reset();
                quotaRepository.save(quota);
            }
            return quota;
        }

        // 创建新配额
        UserDailyFeedQuota quota = new UserDailyFeedQuota();
        quota.setUserId(userId);
        quota.setQuotaDate(today);
        quota.setDailyLimit(DAILY_LIMIT);
        quota.setRemainingCount(DAILY_LIMIT);
        return quotaRepository.save(quota);
    }

    /**
     * 获取需要排除的卡片ID（已读冷却期内）
     */
    private Set<Long> getExcludedCardIds(Long userId) {
        List<Long> coolDownCardIds = readCardRecordRepository
                .findCardIdsInCoolDownPeriod(userId, LocalDateTime.now());
        return new HashSet<>(coolDownCardIds);
    }

    /**
     * 记录卡片已读
     */
    @Transactional
    private void recordCardRead(Long userId, Long cardId) {
        Optional<UserReadCardRecord> existing = readCardRecordRepository
                .findByUserIdAndCardId(userId, cardId);

        if (existing.isPresent()) {
            UserReadCardRecord record = existing.get();
            record.recordRead(COOL_DOWN_DAYS);
            readCardRecordRepository.save(record);
        } else {
            UserReadCardRecord record = new UserReadCardRecord();
            record.setUserId(userId);
            record.setCardId(cardId);
            record.setCoolDownUntil(LocalDateTime.now().plusDays(COOL_DOWN_DAYS));
            readCardRecordRepository.save(record);
        }
    }

    /**
     * 触发后台采集
     */
    private void triggerBackgroundCrawl(Long userId) {
        try {
            List<CrawledContent> pendingContents = crawledContentRepository
                    .findByUserIdAndStatusOrderByCrawledAtDesc(
                            userId, CrawledContent.ContentStatus.RAW);

            if (pendingContents.size() >= 5) {
                log.debug("Triggering background processing for user {}: {} pending contents",
                        userId, pendingContents.size());
            }
        } catch (Exception e) {
            log.error("Error triggering background crawl for user {}", userId, e);
        }
    }

    /**
     * 智能洗牌
     */
    private List<CardResponse> smartShuffle(List<CardResponse> userSource, List<CardResponse> recommended) {
        List<CardResponse> result = new ArrayList<>();
        List<CardResponse> userSourceCopy = new ArrayList<>(userSource);
        List<CardResponse> recommendedCopy = new ArrayList<>(recommended);

        Collections.shuffle(userSourceCopy);
        Collections.shuffle(recommendedCopy);

        int userIdx = 0, recIdx = 0;
        int total = userSource.size() + recommended.size();

        for (int i = 0; i < total; i++) {
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

    // ============ 定时任务 ============

    /**
     * 每日凌晨清理过期记录
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点
    @Transactional
    public void dailyCleanup() {
        log.info("Running daily feed cleanup task...");

        // 1. 清理已过期的已读记录
        int deletedRecords = readCardRecordRepository
                .deleteExpiredRecords(LocalDateTime.now());
        log.info("Deleted {} expired read records", deletedRecords);

        // 2. 归档7天未读的老内容
        archiveOldContent();

        log.info("Daily feed cleanup completed");
    }

    /**
     * 归档老内容
     */
    @Transactional
    public void archiveOldContent() {
        LocalDateTime archiveBefore = LocalDateTime.now().minusDays(ARCHIVE_AFTER_DAYS);

        // 查找7天未读且未归档的卡片
        List<Card> cardsToArchive = cardRepository.findCardsToArchive(archiveBefore);

        for (Card card : cardsToArchive) {
            card.setIsArchived(true);
            card.setArchivedAt(LocalDateTime.now());
            card.setArchiveReason("OLD");
        }

        cardRepository.saveAll(cardsToArchive);
        log.info("Archived {} old cards", cardsToArchive.size());
    }
}
