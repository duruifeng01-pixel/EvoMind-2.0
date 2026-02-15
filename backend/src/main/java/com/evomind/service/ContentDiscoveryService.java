package com.evomind.service;

import com.evomind.entity.CrawledContent;
import com.evomind.entity.Source;

import java.util.List;

/**
 * 内容发现服务接口（30%系统推荐）
 */
public interface ContentDiscoveryService {

    /**
     * 为用户发现新内容（30%推荐）
     */
    List<CrawledContent> discoverContentForUser(Long userId, int limit);

    /**
     * 发现热门趋势内容
     */
    List<CrawledContent> discoverTrendingContent(int limit);

    /**
     * 根据用户画像发现相关内容
     */
    List<CrawledContent> discoverByUserProfile(Long userId, int limit);

    /**
     * 评估内容质量
     */
    int evaluateContentQuality(String title, String content);

    /**
     * 发现优质信息源
     */
    List<Source> discoverQualitySources(String category, int limit);

    /**
     * 获取个性化推荐解释
     */
    String getRecommendationReason(Long userId, Long contentId);
}
