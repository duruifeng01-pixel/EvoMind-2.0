package com.evomind.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 文本相似度计算服务
 * 基于TF-IDF和余弦相似度计算文本间的相似程度
 */
public interface TextSimilarityService {

    /**
     * 计算两段文本的余弦相似度
     * @param text1 文本1
     * @param text2 文本2
     * @return 相似度分数 (0-1)
     */
    BigDecimal calculateCosineSimilarity(String text1, String text2);

    /**
     * 计算TF-IDF向量表示
     * @param text 输入文本
     * @return 词频-逆文档频率映射
     */
    Map<String, Double> calculateTfIdf(String text);

    /**
     * 计算Jaccard相似度（基于关键词集合）
     * @param keywords1 关键词列表1
     * @param keywords2 关键词列表2
     * @return 相似度分数 (0-1)
     */
    BigDecimal calculateJaccardSimilarity(String keywords1, String keywords2);

    /**
     * 批量计算相似度矩阵
     * @param texts 文本列表
     * @return 相似度矩阵
     */
    double[][] calculateSimilarityMatrix(String[] texts);

    /**
     * 提取文本关键词（基于词频）
     * @param text 输入文本
     * @param topN 返回前N个关键词
     * @return 关键词数组
     */
    String[] extractKeywords(String text, int topN);

    /**
     * 判断两段文本是否主题相关
     * @param text1 文本1
     * @param text2 文本2
     * @param threshold 相似度阈值
     * @return 是否相关
     */
    boolean isTopicRelated(String text1, String text2, double threshold);
}
