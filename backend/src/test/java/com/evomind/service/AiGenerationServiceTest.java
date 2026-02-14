package com.evomind.service;

import com.evomind.config.DeepSeekConfig;
import com.evomind.dto.response.AiGeneratedContentResponse;
import com.evomind.service.impl.AiGenerationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AI生成服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AiGenerationServiceTest {

    @Mock
    private DeepSeekConfig deepSeekConfig;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AiGenerationServiceImpl aiGenerationService;

    @BeforeEach
    void setUp() {
        when(deepSeekConfig.getApiKey()).thenReturn("test-api-key");
        when(deepSeekConfig.getBaseUrl()).thenReturn("https://api.deepseek.com");
        when(deepSeekConfig.getModel()).thenReturn("deepseek-chat");
        when(deepSeekConfig.getTemperature()).thenReturn(0.7);
        when(deepSeekConfig.getMaxRetries()).thenReturn(3);
        when(deepSeekConfig.getTimeout()).thenReturn(60);
    }

    @Test
    void testCalculateReadingTime_Chinese() {
        // 测试中文阅读时间计算
        String content = "这是一段测试内容。".repeat(100); // 600个中文字符
        
        int minutes = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "calculateReadingTime", content
        );
        
        assertEquals(2, minutes); // 600字 / 300字每分钟 = 2分钟
    }

    @Test
    void testCalculateReadingTime_English() {
        // 测试英文阅读时间计算
        String content = "This is a test word ".repeat(50); // 约250个英文单词
        
        int minutes = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "calculateReadingTime", content
        );
        
        assertEquals(1, minutes); // 至少1分钟
    }

    @Test
    void testExtractJsonFromResponse_WithCodeBlock() {
        // 测试从代码块中提取JSON
        String response = "```json\n{\"key\": \"value\"}\n```";
        
        String result = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "extractJsonFromResponse", response
        );
        
        assertEquals("{\"key\": \"value\"}", result);
    }

    @Test
    void testExtractJsonFromResponse_WithoutCodeBlock() {
        // 测试直接提取JSON
        String response = "Some text {\"key\": \"value\"} more text";
        
        String result = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "extractJsonFromResponse", response
        );
        
        assertEquals("{\"key\": \"value\"}", result);
    }

    @Test
    void testExtractJsonFromResponse_WithArray() {
        // 测试提取JSON数组
        String response = "Here is the result: [{\"name\": \"test\"}]";
        
        String result = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "extractJsonFromResponse", response
        );
        
        assertEquals("[{\"name\": \"test\"}]", result);
    }

    @Test
    void testTruncateContent() {
        // 测试内容截断
        String content = "a".repeat(5000);
        
        String result = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "truncateContent", content, 100
        );
        
        assertEquals(103, result.length()); // 100 + "..."
        assertTrue(result.endsWith("..."));
    }

    @Test
    void testCountChineseChars() {
        // 测试中文字符计数
        String content = "Hello世界123";
        
        int count = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "countChineseChars", content
        );
        
        assertEquals(2, count); // "世界"
    }

    @Test
    void testParseKeywords() {
        // 测试关键词解析
        String keywordsStr = "认知科学, 学习方法, 大脑可塑性";
        
        @SuppressWarnings("unchecked")
        List<String> result = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "parseKeywords", keywordsStr
        );
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("认知科学", result.get(0));
        assertEquals("学习方法", result.get(1));
        assertEquals("大脑可塑性", result.get(2));
    }

    @Test
    void testEstimateTokenCount() {
        // 测试Token估算
        String content = "Hello世界"; // 2个英文单词 + 2个中文字符
        
        int count = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "estimateTokenCount", content
        );
        
        assertTrue(count > 0);
        // 中文按1:1，英文按1:0.25计算
        // 2个汉字 = 2, 2个英文单词 * 0.25 = 0.5, 总计约2.5
    }

    @Test
    void testBuildOneSentenceSummaryPrompt() {
        // 测试一句话导读Prompt构建
        String content = "这是一篇关于深度学习的文章。深度学习是机器学习的一个分支。";
        
        String prompt = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "buildOneSentenceSummaryPrompt", content
        );
        
        assertNotNull(prompt);
        assertTrue(prompt.contains("一句话"));
        assertTrue(prompt.contains("不超过50字"));
        assertTrue(prompt.contains(content));
    }

    @Test
    void testBuildKeywordExtractionPrompt() {
        // 测试关键词提取Prompt构建
        String content = "人工智能正在改变世界。";
        
        String prompt = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "buildKeywordExtractionPrompt", content
        );
        
        assertNotNull(prompt);
        assertTrue(prompt.contains("5-8个关键词"));
        assertTrue(prompt.contains("逗号分隔"));
    }

    @Test
    void testBuildGoldenQuotePrompt() {
        // 测试金句提取Prompt构建
        String content = "失败是成功之母。坚持就是胜利。";
        
        String prompt = ReflectionTestUtils.invokeMethod(
            aiGenerationService, "buildGoldenQuotePrompt", content
        );
        
        assertNotNull(prompt);
        assertTrue(prompt.contains("3-5个"));
        assertTrue(prompt.contains("金句"));
        assertTrue(prompt.contains("JSON"));
    }

    @Test
    void testAiGeneratedContentResponse_Builder() {
        // 测试响应对象构建
        AiGeneratedContentResponse.GoldenQuote quote = 
            AiGeneratedContentResponse.GoldenQuote.builder()
                .content("测试金句")
                .explanation("这是解释")
                .paragraphIndex(1)
                .build();

        AiGeneratedContentResponse.ExtractedCase caseItem = 
            AiGeneratedContentResponse.ExtractedCase.builder()
                .title("案例标题")
                .content("案例内容")
                .caseType("business")
                .relatedPoint("核心观点")
                .build();

        AiGeneratedContentResponse response = AiGeneratedContentResponse.builder()
                .title("测试标题")
                .oneSentenceSummary("一句话摘要")
                .summaryText("详细摘要")
                .keywords(List.of("关键词1", "关键词2"))
                .readingTimeMinutes(5)
                .goldenQuotes(List.of(quote))
                .cases(List.of(caseItem))
                .mindMapJson("{}")
                .tokenUsed(1000)
                .generationTimeMs(2000L)
                .build();

        assertNotNull(response);
        assertEquals("测试标题", response.getTitle());
        assertEquals("一句话摘要", response.getOneSentenceSummary());
        assertEquals(5, response.getReadingTimeMinutes());
        assertEquals(1, response.getGoldenQuotes().size());
        assertEquals(1, response.getCases().size());
        assertEquals(1000, response.getTokenUsed());
    }
}
