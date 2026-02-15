package com.evomind.service.impl;

import com.evomind.entity.CrawledContent;
import com.evomind.entity.Source;
import com.evomind.entity.UserCognitiveProfile;
import com.evomind.repository.CrawledContentRepository;
import com.evomind.repository.CognitiveProfileRepository;
import com.evomind.repository.SourceRepository;
import com.evomind.service.ContentDiscoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 内容发现服务实现（30%系统推荐）
 * 基于用户画像和学习进度推荐相关内容
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentDiscoveryServiceImpl implements ContentDiscoveryService {

    private final CrawledContentRepository crawledContentRepository;
    private final CognitiveProfileRepository cognitiveProfileRepository;
    private final SourceRepository sourceRepository;

    // 质量评估权重
    private static final double WEIGHT_CONTENT_LENGTH = 0.2;
    private static final double WEIGHT_TITLE_QUALITY = 0.2;
    private static final double WEIGHT_DIVERSITY = 0.3;
    private static final double WEIGHT_FRESHNESS = 0.3;

    // 优质信息源池（系统内置推荐源）
    private static final List<String> QUALITY_SOURCE_POOL = Arrays.asList(
        "https://www.zhihu.com/",
        "https://mp.weixin.qq.com/",
        "https://www.xiaohongshu.com/",
        "https://weibo.com/"
    );

    @Override
    @Transactional
    public List<CrawledContent> discoverContentForUser(Long userId, int limit) {
        log.info("Discovering content for user: {}, limit: {}", userId, limit);

        // 获取用户认知画像（取第一个激活的画像）
        List<UserCognitiveProfile> profiles = cognitiveProfileRepository.findByUserIdAndIsActiveTrue(userId);
        Optional<UserCognitiveProfile> profileOpt = profiles.isEmpty() ?
                Optional.empty() : Optional.of(profiles.get(0));
        
        List<CrawledContent> discoveredContent = new ArrayList<>();

        if (profileOpt.isPresent()) {
            UserCognitiveProfile profile = profileOpt.get();
            
            // 1. 基于兴趣标签发现内容
            discoveredContent.addAll(discoverByUserProfile(userId, limit / 2));
            
            // 2. 发现热门趋势内容（补充）
            int remainingLimit = limit - discoveredContent.size();
            if (remainingLimit > 0) {
                discoveredContent.addAll(discoverTrendingContent(remainingLimit));
            }
        } else {
            // 用户没有画像，返回热门趋势
            discoveredContent.addAll(discoverTrendingContent(limit));
        }

        // 去重并限制数量
        return discoveredContent.stream()
                .distinct()
                .limit(limit)
                .peek(content -> content.setIsSystemDiscovered(true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<CrawledContent> discoverTrendingContent(int limit) {
        log.info("Discovering trending content, limit: {}", limit);

        // 获取最近24小时内采集的、质量较高的内容
        Pageable pageable = PageRequest.of(0, limit * 2);
        
        return crawledContentRepository.findSystemDiscoveredContent(pageable).stream()
                .filter(content -> content.getQualityScore() >= 60)  // 质量分数>=60
                .sorted((a, b) -> Integer.compare(b.getQualityScore(), a.getQualityScore()))
                .limit(limit)
                .peek(content -> {
                    content.setIsSystemDiscovered(true);
                    crawledContentRepository.save(content);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<CrawledContent> discoverByUserProfile(Long userId, int limit) {
        log.info("Discovering content by user profile: {}, limit: {}", userId, limit);

        Optional<UserCognitiveProfile> profileOpt = cognitiveProfileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            log.warn("No cognitive profile found for user: {}", userId);
            return Collections.emptyList();
        }

        UserCognitiveProfile profile = profileOpt.get();
        
        // 解析用户兴趣标签
        List<String> interests = parseInterests(profile);
        
        if (interests.isEmpty()) {
            log.warn("No interests found for user: {}", userId);
            return Collections.emptyList();
        }

        List<CrawledContent> matchedContent = new ArrayList<>();
        
        // 基于每个兴趣标签搜索相关内容
        for (String interest : interests) {
            Pageable pageable = PageRequest.of(0, limit / interests.size() + 1);
            
            // 从已采集的内容中匹配
            List<CrawledContent> matching = crawledContentRepository
                    .findSystemDiscoveredContent(pageable).stream()
                    .filter(content -> matchesInterest(content, interest))
                    .filter(content -> content.getQualityScore() >= 50)
                    .limit(limit / interests.size())
                    .peek(content -> {
                        content.setIsSystemDiscovered(true);
                        crawledContentRepository.save(content);
                    })
                    .toList();
            
            matchedContent.addAll(matching);
        }

        return matchedContent.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public int evaluateContentQuality(String title, String content) {
        if (title == null || content == null) {
            return 0;
        }

        int score = 50;  // 基础分

        // 1. 内容长度评分
        int contentLength = content.length();
        if (contentLength > 2000) {
            score += 20;
        } else if (contentLength > 1000) {
            score += 10;
        } else if (contentLength < 200) {
            score -= 20;
        }

        // 2. 标题质量评分
        if (title.length() >= 10 && title.length() <= 100) {
            score += 10;
        }
        
        // 检查标题是否包含垃圾关键词
        if (containsSpamKeywords(title)) {
            score -= 30;
        }

        // 3. 内容多样性评分
        if (hasGoodDiversity(content)) {
            score += 15;
        }

        // 4. 时效性评分
        score += 5;  // 新内容默认加分

        return Math.max(0, Math.min(100, score));
    }

    @Override
    @Transactional
    public List<Source> discoverQualitySources(String category, int limit) {
        log.info("Discovering quality sources for category: {}, limit: {}", category, limit);

        // 根据分类推荐优质信息源
        List<Source> sources = new ArrayList<>();
        
        // 这里可以实现基于分类的源推荐逻辑
        // 暂时返回空列表，实际应从配置或数据库加载
        
        return sources.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public String getRecommendationReason(Long userId, Long contentId) {
        List<UserCognitiveProfile> profiles = cognitiveProfileRepository.findByUserIdAndIsActiveTrue(userId);
        Optional<UserCognitiveProfile> profileOpt = profiles.isEmpty() ?
                Optional.empty() : Optional.of(profiles.get(0));
        Optional<CrawledContent> contentOpt = crawledContentRepository.findById(contentId);

        if (profileOpt.isEmpty() || contentOpt.isEmpty()) {
            return "根据您的阅读习惯推荐";
        }

        UserCognitiveProfile profile = profileOpt.get();
        CrawledContent content = contentOpt.get();

        // 分析推荐理由
        List<String> reasons = new ArrayList<>();
        
        List<String> interests = parseInterests(profile);
        for (String interest : interests) {
            if (matchesInterest(content, interest)) {
                reasons.add("与您的「" + interest + "」兴趣相关");
                break;
            }
        }

        if (content.getQualityScore() >= 80) {
            reasons.add("高质量内容");
        }

        if (content.getCrawledAt() != null && 
            content.getCrawledAt().isAfter(LocalDateTime.now().minusDays(7))) {
            reasons.add("近期热门");
        }

        if (reasons.isEmpty()) {
            return "拓展您的阅读视野";
        }

        return String.join(" · ", reasons);
    }

    /**
     * 解析用户兴趣标签
     */
    private List<String> parseInterests(UserCognitiveProfile profile) {
        List<String> interests = new ArrayList<>();
        
        // 从主题解析
        if (profile.getTopic() != null) {
            interests.add(profile.getTopic());
        }

        // 从关键词解析
        if (profile.getKeywords() != null) {
            String[] keywords = profile.getKeywords().split(",");
            interests.addAll(Arrays.asList(keywords));
        }

        return interests.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(5)  // 最多取5个兴趣标签
                .collect(Collectors.toList());
    }

    /**
     * 判断内容是否匹配兴趣标签
     */
    private boolean matchesInterest(CrawledContent content, String interest) {
        if (content == null || interest == null) {
            return false;
        }

        String lowerInterest = interest.toLowerCase();
        
        // 检查标题
        if (content.getTitle() != null && 
            content.getTitle().toLowerCase().contains(lowerInterest)) {
            return true;
        }

        // 检查内容
        if (content.getContent() != null && 
            content.getContent().toLowerCase().contains(lowerInterest)) {
            return true;
        }

        // 检查分类
        if (content.getCategory() != null && 
            content.getCategory().toLowerCase().contains(lowerInterest)) {
            return true;
        }

        // 检查标签
        if (content.getTags() != null && 
            content.getTags().toLowerCase().contains(lowerInterest)) {
            return true;
        }

        return false;
    }

    /**
     * 检查标题是否包含垃圾关键词
     */
    private boolean containsSpamKeywords(String title) {
        String[] spamKeywords = {
            "震惊", "不转不是", "点击领取", "免费送", "限时",
            "绝对", "一定", "肯定", "必然"
        };
        
        String lowerTitle = title.toLowerCase();
        for (String keyword : spamKeywords) {
            if (lowerTitle.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断内容是否有良好的多样性
     */
    private boolean hasGoodDiversity(String content) {
        if (content == null || content.length() < 100) {
            return false;
        }

        // 检查是否包含多种标点符号
        int punctuationCount = 0;
        if (content.contains("，")) punctuationCount++;
        if (content.contains("。")) punctuationCount++;
        if (content.contains("；")) punctuationCount++;
        if (content.contains("？")) punctuationCount++;
        if (content.contains("！")) punctuationCount++;

        return punctuationCount >= 3;
    }

    /**
     * 计算多样性得分
     */
    private double calculateDiversityScore(String content) {
        if (content == null || content.isEmpty()) {
            return 0.0;
        }

        // 使用简单的词汇多样性计算
        String[] words = content.split("\\s+");
        if (words.length == 0) return 0.0;

        Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
        return (double) uniqueWords.size() / words.length;
    }
}
