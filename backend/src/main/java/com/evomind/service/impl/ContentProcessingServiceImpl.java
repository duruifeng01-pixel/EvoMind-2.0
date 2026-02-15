package com.evomind.service.impl;

import com.evomind.dto.request.CreateCardRequest;
import com.evomind.entity.Card;
import com.evomind.entity.CrawledContent;
import com.evomind.entity.UserCognitiveProfile;
import com.evomind.repository.CardRepository;
import com.evomind.repository.CrawledContentRepository;
import com.evomind.repository.UserCognitiveProfileRepository;
import com.evomind.service.CardService;
import com.evomind.service.AiAnalysisService;
import com.evomind.service.ContentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容处理服务实现
 * 将采集的内容转换为认知卡片
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentProcessingServiceImpl implements ContentProcessingService {

    private final CrawledContentRepository crawledContentRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final AiAnalysisService aiAnalysisService;
    private final UserCognitiveProfileRepository cognitiveProfileRepository;

    @Override
    @Transactional
    public Card processToCard(Long crawledContentId) {
        CrawledContent crawledContent = crawledContentRepository.findById(crawledContentId)
                .orElseThrow(() -> new RuntimeException("Crawled content not found: " + crawledContentId));

        // 检查是否已处理
        if (crawledContent.getCardId() != null) {
            log.info("Content already processed to card: {}", crawledContent.getCardId());
            return cardRepository.findById(crawledContent.getCardId()).orElse(null);
        }

        try {
            // 更新状态为处理中
            crawledContent.setStatus(CrawledContent.ContentStatus.PROCESSING);
            crawledContentRepository.save(crawledContent);

            // 1. 自动分类
            String category = autoClassify(crawledContent.getTitle(), crawledContent.getContent());
            crawledContent.setCategory(category);

            // 2. 提取关键词
            List<String> keywords = extractKeywords(crawledContent.getTitle(), crawledContent.getContent());
            crawledContent.setTags(String.join(",", keywords));

            // 3. 提取核心观点
            String coreOpinion = extractCoreOpinion(crawledContent.getContent());

            // 4. 评估重要性
            int importance = evaluateImportance(crawledContent.getContent(), category);

            // 5. 生成脑图结构
            String mindMapStructure = generateMindMapStructure(
                    crawledContent.getTitle(), crawledContent.getContent());

            // 6. 创建卡片请求
            CreateCardRequest request = new CreateCardRequest();
            request.setTitle(crawledContent.getTitle());
            request.setContent(coreOpinion);
            request.setOriginalText(crawledContent.getContent());
            request.setCategory(category);
            request.setTags(keywords);
            request.setSourceUrl(crawledContent.getOriginalUrl());
            request.setAuthor(crawledContent.getAuthor());
            request.setPublishedAt(crawledContent.getPublishedAt());
            request.setImportanceLevel(importance >= 70 ? "HIGH" : importance >= 40 ? "MEDIUM" : "LOW");
            request.setMindMapStructure(mindMapStructure);

            // 7. 创建卡片
            Card card = cardService.createCard(crawledContent.getUserId(), request);

            // 8. 更新采集内容状态
            crawledContent.setStatus(CrawledContent.ContentStatus.COMPLETED);
            crawledContent.setCardId(card.getId());
            crawledContent.setQualityScore(importance);
            crawledContentRepository.save(crawledContent);

            log.info("Successfully processed content to card: contentId={}, cardId={}",
                    crawledContentId, card.getId());

            return card;

        } catch (Exception e) {
            log.error("Failed to process content to card: {}", crawledContentId, e);
            
            crawledContent.setStatus(CrawledContent.ContentStatus.FAILED);
            crawledContentRepository.save(crawledContent);
            
            throw new RuntimeException("Content processing failed", e);
        }
    }

    @Override
    @Transactional
    public List<Card> batchProcessToCards(List<Long> crawledContentIds) {
        List<Card> cards = new ArrayList<>();
        
        for (Long contentId : crawledContentIds) {
            try {
                Card card = processToCard(contentId);
                if (card != null) {
                    cards.add(card);
                }
            } catch (Exception e) {
                log.error("Failed to process content: {}", contentId, e);
            }
        }

        return cards;
    }

    @Override
    public String extractCoreOpinion(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        try {
            // 使用AI分析提取核心观点
            // 这里简化处理，实际应调用aiAnalysisService
            
            // 简单策略：取前500个字符作为摘要
            if (content.length() <= 500) {
                return content;
            }

            // 尝试找到第一段完整的话
            int firstParagraphEnd = content.indexOf("\n\n");
            if (firstParagraphEnd > 100 && firstParagraphEnd < 500) {
                return content.substring(0, firstParagraphEnd).trim();
            }

            // 否则取前500字符
            return content.substring(0, 500) + "...";

        } catch (Exception e) {
            log.error("Error extracting core opinion", e);
            return content.length() > 500 ? content.substring(0, 500) + "..." : content;
        }
    }

    @Override
    public String generateMindMapStructure(String title, String content) {
        // 生成简单的脑图结构（JSON格式）
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"root\":\"").append(escapeJson(title)).append("\",");
        sb.append("\"nodes\":[");
        
        // 添加主要节点
        String[] paragraphs = content.split("\n\n");
        for (int i = 0; i < Math.min(paragraphs.length, 5); i++) {
            if (i > 0) sb.append(",");
            String paragraph = paragraphs[i].trim();
            if (paragraph.length() > 50) {
                paragraph = paragraph.substring(0, 50) + "...";
            }
            sb.append("{");
            sb.append("\"id\":\"node").append(i).append("\",");
            sb.append("\"text\":\"").append(escapeJson(paragraph)).append("\",");
            sb.append("\"level\":1");
            sb.append("}");
        }
        
        sb.append("]}");
        return sb.toString();
    }

    @Override
    public int evaluateImportance(String content, String category) {
        int score = 50; // 基础分

        // 内容长度评分
        int length = content != null ? content.length() : 0;
        if (length > 3000) {
            score += 20;
        } else if (length > 1500) {
            score += 10;
        } else if (length < 300) {
            score -= 20;
        }

        // 关键词丰富度评分
        if (content != null) {
            int punctuationCount = countPunctuation(content);
            if (punctuationCount > 10) {
                score += 10;
            }

            // 专业术语加分
            String[] professionalTerms = {"分析", "研究", "数据", "报告", "结论", "观点", "理论"};
            for (String term : professionalTerms) {
                if (content.contains(term)) {
                    score += 2;
                }
            }
        }

        // 分类权重
        if ("TECHNOLOGY".equals(category) || "BUSINESS".equals(category)) {
            score += 5;
        }

        return Math.max(0, Math.min(100, score));
    }

    @Override
    public String autoClassify(String title, String content) {
        String text = (title + " " + content).toLowerCase();

        // 简单规则分类
        if (containsAny(text, "科技", "技术", "AI", "人工智能", "编程", "软件", "互联网")) {
            return "TECHNOLOGY";
        }
        if (containsAny(text, "商业", "经济", "投资", "创业", "市场", "金融")) {
            return "BUSINESS";
        }
        if (containsAny(text, "健康", "医疗", "养生", "健身", "心理")) {
            return "HEALTH";
        }
        if (containsAny(text, "教育", "学习", "知识", "读书", "课程")) {
            return "EDUCATION";
        }
        if (containsAny(text, "设计", "艺术", "创意", "美学", "摄影")) {
            return "DESIGN";
        }
        if (containsAny(text, "生活", "旅行", "美食", "家居", "时尚")) {
            return "LIFESTYLE";
        }

        return "GENERAL";
    }

    @Override
    public List<String> extractKeywords(String title, String content) {
        List<String> keywords = new ArrayList<>();
        String text = (title + " " + content).toLowerCase();

        // 预定义关键词库
        String[][] keywordCategories = {
            {"人工智能", "机器学习", "深度学习", "神经网络"},
            {"大数据", "云计算", "区块链", "物联网"},
            {"创业", "投资", "融资", "商业模式"},
            {"心理学", "认知科学", "行为经济学"},
            {"产品", "运营", "营销", "增长"}
        };

        for (String[] category : keywordCategories) {
            for (String keyword : category) {
                if (text.contains(keyword)) {
                    keywords.add(keyword);
                    if (keywords.size() >= 5) {
                        return keywords;
                    }
                    break;
                }
            }
        }

        // 如果没有匹配到关键词，从标题中提取前5个词
        if (keywords.isEmpty() && title != null) {
            String[] words = title.split("\\s+|，|。");
            for (String word : words) {
                if (word.length() >= 2) {
                    keywords.add(word);
                    if (keywords.size() >= 5) {
                        break;
                    }
                }
            }
        }

        return keywords.stream().distinct().limit(5).collect(Collectors.toList());
    }

    @Override
    public List<CrawledContent> getPendingContents(Long userId, int limit) {
        return crawledContentRepository.findByUserIdAndStatusOrderByCrawledAtDesc(
                userId, CrawledContent.ContentStatus.DEDUPLICATED)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 辅助方法
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private int countPunctuation(String text) {
        int count = 0;
        String punctuation = "。，；：！？、\"'";
        for (char c : text.toCharArray()) {
            if (punctuation.indexOf(c) >= 0) {
                count++;
            }
        }
        return count;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
