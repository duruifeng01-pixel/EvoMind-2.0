package com.evomind.service.impl;

import com.evomind.entity.Card;
import com.evomind.entity.UserInterestProfile;
import com.evomind.entity.UserReadingHistory;
import com.evomind.repository.CardRepository;
import com.evomind.repository.UserInterestProfileRepository;
import com.evomind.repository.UserReadingHistoryRepository;
import com.evomind.service.RecommendationService;
import com.evomind.service.TextSimilarityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserInterestProfileRepository interestProfileRepository;
    private final UserReadingHistoryRepository readingHistoryRepository;
    private final CardRepository cardRepository;
    private final TextSimilarityService textSimilarityService;

    @Override
    public List<Card> recommendCards(Long userId, int limit, List<Long> excludeCardIds) {
        // 1. 获取用户兴趣画像
        List<UserInterestProfile> userInterests = interestProfileRepository
                .findByUserIdAndIsActiveTrue(userId);

        if (userInterests.isEmpty()) {
            // 新用户：返回热门/最新内容
            return getDefaultRecommendations(limit, excludeCardIds);
        }

        // 2. 按权重排序的兴趣标签
        List<String> topTags = userInterests.stream()
                .sorted((a, b) -> b.calculateScore().compareTo(a.calculateScore()))
                .limit(10)
                .map(UserInterestProfile::getInterestTag)
                .collect(Collectors.toList());

        // 3. 基于关键词匹配推荐
        List<Card> recommended = new ArrayList<>();
        Set<Long> addedCardIds = new HashSet<>(excludeCardIds);

        for (String tag : topTags) {
            if (recommended.size() >= limit) break;

            List<Card> matchingCards = cardRepository.findByKeywordsContainingAndIdNotIn(
                    tag, addedCardIds, PageRequest.of(0, limit - recommended.size()));

            for (Card card : matchingCards) {
                if (!addedCardIds.contains(card.getId())) {
                    recommended.add(card);
                    addedCardIds.add(card.getId());
                }
            }
        }

        // 4. 如果推荐不足，补充多样性内容
        if (recommended.size() < limit) {
            List<Card> diverseCards = getDiverseRecommendations(userId, limit - recommended.size());
            for (Card card : diverseCards) {
                if (!addedCardIds.contains(card.getId())) {
                    recommended.add(card);
                    addedCardIds.add(card.getId());
                }
            }
        }

        return recommended.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void buildUserInterestProfile(Long userId) {
        // 获取用户近30天的阅读历史
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<String> recentKeywords = readingHistoryRepository.findRecentKeywords(userId, since);

        // 统计关键词频率
        Map<String, Integer> keywordFreq = new HashMap<>();
        for (String keywords : recentKeywords) {
            if (keywords != null) {
                for (String kw : keywords.split(",")) {
                    keywordFreq.merge(kw.trim(), 1, Integer::sum);
                }
            }
        }

        // 更新兴趣画像
        int totalInteractions = keywordFreq.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : keywordFreq.entrySet()) {
            String tag = entry.getKey();
            int count = entry.getValue();

            BigDecimal weight = BigDecimal.valueOf((double) count / totalInteractions)
                    .setScale(4, RoundingMode.HALF_UP);

            Optional<UserInterestProfile> existing = interestProfileRepository
                    .findByUserIdAndInterestTag(userId, tag);

            if (existing.isPresent()) {
                UserInterestProfile profile = existing.get();
                profile.setWeight(weight);
                profile.setInteractionScore(count);
                profile.setLastInteractionAt(LocalDateTime.now());
                interestProfileRepository.save(profile);
            } else {
                UserInterestProfile profile = new UserInterestProfile();
                profile.setUserId(userId);
                profile.setInterestTag(tag);
                profile.setWeight(weight);
                profile.setInteractionScore(count);
                profile.setLastInteractionAt(LocalDateTime.now());
                interestProfileRepository.save(profile);
            }
        }
    }

    @Override
    @Transactional
    public void updateInterestWeight(Long userId, Long cardId, String keywords, int score) {
        if (keywords == null || keywords.isEmpty()) return;

        String[] tags = keywords.split(",");
        for (String tag : tags) {
            tag = tag.trim();
            if (tag.isEmpty()) continue;

            Optional<UserInterestProfile> existing = interestProfileRepository
                    .findByUserIdAndInterestTag(userId, tag);

            if (existing.isPresent()) {
                UserInterestProfile profile = existing.get();
                BigDecimal newWeight = profile.getWeight()
                        .add(BigDecimal.valueOf(score).multiply(BigDecimal.valueOf(0.01)))
                        .min(BigDecimal.ONE);
                profile.setWeight(newWeight);
                profile.setInteractionScore(profile.getInteractionScore() + score);
                profile.setLastInteractionAt(LocalDateTime.now());
                interestProfileRepository.save(profile);
            } else {
                UserInterestProfile profile = new UserInterestProfile();
                profile.setUserId(userId);
                profile.setInterestTag(tag);
                profile.setWeight(BigDecimal.valueOf(Math.min(score * 0.01, 0.5)));
                profile.setInteractionScore(score);
                profile.setLastInteractionAt(LocalDateTime.now());
                interestProfileRepository.save(profile);
            }
        }
    }

    @Override
    public List<UserInterestProfile> getUserTopInterests(Long userId, int topN) {
        return interestProfileRepository.findTopInterestsByUserId(
                userId, PageRequest.of(0, topN));
    }

    @Override
    public double calculateEchoChamberRisk(Long userId) {
        List<UserInterestProfile> interests = interestProfileRepository
                .findByUserIdAndIsActiveTrue(userId);

        if (interests.size() < 3) return 0.0; // 兴趣点太少，不算茧房

        // 计算兴趣分布的熵（多样性指标）
        double totalWeight = interests.stream()
                .mapToDouble(i -> i.getWeight().doubleValue())
                .sum();

        double entropy = 0.0;
        for (UserInterestProfile interest : interests) {
            double p = interest.getWeight().doubleValue() / totalWeight;
            if (p > 0) {
                entropy -= p * Math.log(p);
            }
        }

        // 归一化熵（最大熵是ln(N)）
        double maxEntropy = Math.log(interests.size());
        double normalizedEntropy = entropy / maxEntropy;

        // 风险分数（熵越低，风险越高）
        return 1.0 - normalizedEntropy;
    }

    @Override
    public List<Card> getDiverseRecommendations(Long userId, int limit) {
        // 1. 获取用户已有的兴趣类别
        List<Object[]> categoryDist = interestProfileRepository
                .findCategoryDistributionByUserId(userId);

        Set<String> existingCategories = categoryDist.stream()
                .map(arr -> (String) arr[0])
                .collect(Collectors.toSet());

        // 2. 获取未探索类别的内容
        List<Card> diverseCards = cardRepository.findByCategoryNotIn(
                existingCategories, PageRequest.of(0, limit));

        // 3. 如果不足，随机补充
        if (diverseCards.size() < limit) {
            List<Card> randomCards = cardRepository.findRandomCards(
                    limit - diverseCards.size());
            diverseCards.addAll(randomCards);
        }

        return diverseCards.stream().limit(limit).collect(Collectors.toList());
    }

    private List<Card> getDefaultRecommendations(int limit, List<Long> excludeCardIds) {
        // 返回最新的、热门的卡片
        return cardRepository.findByIdNotInOrderByCreatedAtDesc(
                excludeCardIds, PageRequest.of(0, limit));
    }
}
