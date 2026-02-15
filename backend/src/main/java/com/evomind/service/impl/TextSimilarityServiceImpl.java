package com.evomind.service.impl;

import com.evomind.service.TextSimilarityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本相似度计算服务实现
 * 基于TF-IDF和余弦相似度
 */
@Slf4j
@Service
public class TextSimilarityServiceImpl implements TextSimilarityService {

    // 停用词列表（中文和英文）
    private static final Set<String> STOP_WORDS = Set.of(
        "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也",
        "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这", "那",
        "the", "a", "an", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
        "do", "does", "did", "will", "would", "could", "should", "may", "might", "must", "can",
        "this", "that", "these", "those", "and", "or", "but", "in", "on", "at", "to", "for",
        "of", "with", "by", "from", "as", "it", "its", "they", "them", "their", "we", "us", "our"
    );

    private static final Pattern WORD_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]+|[a-zA-Z]+");

    @Override
    public BigDecimal calculateCosineSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isBlank() || text2.isBlank()) {
            return BigDecimal.ZERO;
        }

        Map<String, Double> tfIdf1 = calculateTfIdf(text1);
        Map<String, Double> tfIdf2 = calculateTfIdf(text2);

        // 合并所有词
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(tfIdf1.keySet());
        allTerms.addAll(tfIdf2.keySet());

        // 计算点积
        double dotProduct = 0.0;
        for (String term : allTerms) {
            double v1 = tfIdf1.getOrDefault(term, 0.0);
            double v2 = tfIdf2.getOrDefault(term, 0.0);
            dotProduct += v1 * v2;
        }

        // 计算模长
        double norm1 = Math.sqrt(tfIdf1.values().stream().mapToDouble(v -> v * v).sum());
        double norm2 = Math.sqrt(tfIdf2.values().stream().mapToDouble(v -> v * v).sum());

        if (norm1 == 0.0 || norm2 == 0.0) {
            return BigDecimal.ZERO;
        }

        double similarity = dotProduct / (norm1 * norm2);
        return BigDecimal.valueOf(similarity).setScale(4, RoundingMode.HALF_UP);
    }

    @Override
    public Map<String, Double> calculateTfIdf(String text) {
        List<String> tokens = tokenize(text);
        
        // 计算词频TF
        Map<String, Integer> termFrequency = new HashMap<>();
        for (String token : tokens) {
            termFrequency.merge(token, 1, Integer::sum);
        }

        // 简化版TF-IDF（单文档场景，IDF设为1）
        Map<String, Double> tfIdf = new HashMap<>();
        int totalTerms = tokens.size();
        
        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            double tf = (double) entry.getValue() / totalTerms;
            double idf = 1.0; // 单文档简化处理
            tfIdf.put(entry.getKey(), tf * idf);
        }

        return tfIdf;
    }

    @Override
    public BigDecimal calculateJaccardSimilarity(String keywords1, String keywords2) {
        if (keywords1 == null || keywords2 == null) {
            return BigDecimal.ZERO;
        }

        Set<String> set1 = Arrays.stream(keywords1.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> set2 = Arrays.stream(keywords2.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (set1.isEmpty() || set2.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 计算交集和并集
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        double similarity = (double) intersection.size() / union.size();
        return BigDecimal.valueOf(similarity).setScale(4, RoundingMode.HALF_UP);
    }

    @Override
    public double[][] calculateSimilarityMatrix(String[] texts) {
        int n = texts.length;
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 1.0;
                } else {
                    BigDecimal similarity = calculateCosineSimilarity(texts[i], texts[j]);
                    matrix[i][j] = matrix[j][i] = similarity.doubleValue();
                }
            }
        }

        return matrix;
    }

    @Override
    public String[] extractKeywords(String text, int topN) {
        if (text == null || text.isBlank()) {
            return new String[0];
        }

        List<String> tokens = tokenize(text);
        
        // 统计词频
        Map<String, Integer> frequency = new HashMap<>();
        for (String token : tokens) {
            frequency.merge(token, 1, Integer::sum);
        }

        // 按频率排序，取前N个
        return frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }

    @Override
    public boolean isTopicRelated(String text1, String text2, double threshold) {
        BigDecimal similarity = calculateCosineSimilarity(text1, text2);
        return similarity.doubleValue() >= threshold;
    }

    /**
     * 分词处理
     */
    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        
        // 中文分词（简化版：按字符分词）
        for (char c : text.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                String token = String.valueOf(c).toLowerCase();
                if (!STOP_WORDS.contains(token)) {
                    tokens.add(token);
                }
            }
        }

        // 英文单词提取
        var matcher = WORD_PATTERN.matcher(text.toLowerCase());
        while (matcher.find()) {
            String token = matcher.group();
            if (token.length() > 1 && !STOP_WORDS.contains(token)) {
                tokens.add(token);
            }
        }

        return tokens;
    }
}
