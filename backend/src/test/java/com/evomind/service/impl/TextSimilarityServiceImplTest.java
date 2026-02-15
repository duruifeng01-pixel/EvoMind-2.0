package com.evomind.service.impl;

import com.evomind.service.TextSimilarityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文本相似度服务测试
 */
class TextSimilarityServiceImplTest {

    private TextSimilarityService textSimilarityService;

    @BeforeEach
    void setUp() {
        textSimilarityService = new TextSimilarityServiceImpl();
    }

    @Test
    void testCalculateCosineSimilarity_SameText() {
        String text = "人工智能正在改变世界";
        BigDecimal similarity = textSimilarityService.calculateCosineSimilarity(text, text);
        
        assertTrue(similarity.doubleValue() > 0.99, "相同文本应该高度相似");
    }

    @Test
    void testCalculateCosineSimilarity_DifferentText() {
        String text1 = "人工智能正在改变世界";
        String text2 = "今天的天气很好";
        BigDecimal similarity = textSimilarityService.calculateCosineSimilarity(text1, text2);
        
        assertTrue(similarity.doubleValue() < 0.5, "不相关文本应该低相似度");
    }

    @Test
    void testCalculateCosineSimilarity_RelatedText() {
        String text1 = "机器学习是人工智能的一个分支";
        String text2 = "深度学习属于机器学习领域";
        BigDecimal similarity = textSimilarityService.calculateCosineSimilarity(text1, text2);
        
        assertTrue(similarity.doubleValue() > 0.3, "相关文本应该有中等相似度");
    }

    @Test
    void testCalculateCosineSimilarity_EmptyText() {
        BigDecimal similarity = textSimilarityService.calculateCosineSimilarity("", "some text");
        assertEquals(0.0, similarity.doubleValue(), "空文本应该返回0相似度");
    }

    @Test
    void testCalculateTfIdf() {
        String text = "人工智能 人工智能 机器学习";
        Map<String, Double> tfIdf = textSimilarityService.calculateTfIdf(text);
        
        assertFalse(tfIdf.isEmpty(), "TF-IDF不应该为空");
        assertTrue(tfIdf.containsKey("人"), "应该包含中文字符");
        assertTrue(tfIdf.containsKey("智能"), "应该包含英文单词");
    }

    @Test
    void testCalculateJaccardSimilarity() {
        String keywords1 = "人工智能,机器学习,深度学习";
        String keywords2 = "机器学习,深度学习,神经网络";
        
        BigDecimal similarity = textSimilarityService.calculateJaccardSimilarity(keywords1, keywords2);
        
        assertTrue(similarity.doubleValue() > 0.3, "应该有部分关键词重叠");
        assertTrue(similarity.doubleValue() < 0.8, "不应该完全重叠");
    }

    @Test
    void testCalculateJaccardSimilarity_NoOverlap() {
        String keywords1 = "人工智能,机器学习";
        String keywords2 = "经济学,金融学";
        
        BigDecimal similarity = textSimilarityService.calculateJaccardSimilarity(keywords1, keywords2);
        
        assertEquals(0.0, similarity.doubleValue(), "无重叠关键词应该返回0");
    }

    @Test
    void testExtractKeywords() {
        String text = "人工智能正在快速发展，人工智能技术正在改变我们的生活方式。";
        String[] keywords = textSimilarityService.extractKeywords(text, 5);
        
        assertTrue(keywords.length <= 5, "返回的关键词数量应该不超过请求数量");
        assertTrue(keywords.length > 0, "应该提取到关键词");
    }

    @Test
    void testIsTopicRelated() {
        String text1 = "机器学习是人工智能的核心技术";
        String text2 = "深度学习在图像识别中的应用";
        
        boolean related = textSimilarityService.isTopicRelated(text1, text2, 0.3);
        
        assertTrue(related, "相关主题应该被识别为相关");
    }

    @Test
    void testCalculateSimilarityMatrix() {
        String[] texts = {
            "人工智能",
            "机器学习",
            "今天的天气"
        };
        
        double[][] matrix = textSimilarityService.calculateSimilarityMatrix(texts);
        
        assertEquals(3, matrix.length, "矩阵维度应该与文本数量一致");
        assertEquals(3, matrix[0].length, "矩阵应该是方阵");
        assertEquals(1.0, matrix[0][0], "对角线应该为1");
        assertEquals(matrix[0][1], matrix[1][0], "矩阵应该对称");
    }
}
