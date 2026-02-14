package com.evomind.service.impl;

import com.evomind.config.DeepSeekConfig;
import com.evomind.dto.response.AiGeneratedContentResponse;
import com.evomind.service.AiGenerationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI内容生成服务实现 - DeepSeek API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationServiceImpl implements AiGenerationService {

    private final DeepSeekConfig deepSeekConfig;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_ENDPOINT = "/chat/completions";

    @Override
    public AiGeneratedContentResponse generateCardContent(String originalContent, String title, boolean generateMindMap) {
        long startTime = System.currentTimeMillis();
        log.info("开始生成认知卡片内容, title={}", title);

        try {
            // 1. 生成一句话导读
            String oneSentenceSummary = generateOneSentenceSummary(originalContent);
            
            // 2. 生成核心观点摘要
            String summaryText = generateSummary(originalContent);
            
            // 3. 提取关键词
            String keywordsStr = extractKeywords(originalContent);
            List<String> keywords = parseKeywords(keywordsStr);
            
            // 4. 计算阅读时长
            int readingTimeMinutes = calculateReadingTime(originalContent);
            
            // 5. 提取金句
            List<AiGeneratedContentResponse.GoldenQuote> goldenQuotes = parseGoldenQuotes(
                extractGoldenQuotes(originalContent)
            );
            
            // 6. 提取案例
            List<AiGeneratedContentResponse.ExtractedCase> cases = parseCases(
                extractCases(originalContent)
            );
            
            // 7. 生成脑图
            String mindMapJson = null;
            if (generateMindMap) {
                mindMapJson = generateMindMap(summaryText, oneSentenceSummary);
            }

            long generationTimeMs = System.currentTimeMillis() - startTime;
            
            // 估算Token使用量 (中文字符约1:1.5，英文约1:4)
            int tokenUsed = estimateTokenCount(originalContent) + 1500;

            log.info("认知卡片生成完成, 耗时={}ms, token={}", generationTimeMs, tokenUsed);

            return AiGeneratedContentResponse.builder()
                    .title(title != null ? title : generateTitle(originalContent))
                    .oneSentenceSummary(oneSentenceSummary)
                    .summaryText(summaryText)
                    .keywords(keywords)
                    .readingTimeMinutes(readingTimeMinutes)
                    .goldenQuotes(goldenQuotes)
                    .cases(cases)
                    .mindMapJson(mindMapJson)
                    .tokenUsed(tokenUsed)
                    .generationTimeMs(generationTimeMs)
                    .build();

        } catch (Exception e) {
            log.error("生成认知卡片内容失败", e);
            throw new RuntimeException("AI生成失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateMindMap(String summaryText, String keyPoints) {
        String prompt = buildMindMapPrompt(summaryText, keyPoints);
        String response = callDeepSeekApi(prompt, 2000);
        return extractJsonFromResponse(response);
    }

    @Override
    public String extractGoldenQuotes(String content) {
        String prompt = buildGoldenQuotePrompt(content);
        return callDeepSeekApi(prompt, 1500);
    }

    @Override
    public String extractCases(String content) {
        String prompt = buildCaseExtractionPrompt(content);
        return callDeepSeekApi(prompt, 2000);
    }

    @Override
    public String generateOneSentenceSummary(String content) {
        String prompt = buildOneSentenceSummaryPrompt(content);
        return callDeepSeekApi(prompt, 200);
    }

    @Override
    public String extractKeywords(String content) {
        String prompt = buildKeywordExtractionPrompt(content);
        return callDeepSeekApi(prompt, 200);
    }

    /**
     * 生成文章标题
     */
    private String generateTitle(String content) {
        String prompt = "请为以下内容生成一个简洁有力的标题（不超过15个字），只返回标题本身：\n\n" + 
                       content.substring(0, Math.min(content.length(), 500));
        return callDeepSeekApi(prompt, 50);
    }

    /**
     * 生成核心观点摘要
     */
    private String generateSummary(String content) {
        String prompt = buildSummaryPrompt(content);
        return callDeepSeekApi(prompt, 1000);
    }

    /**
     * 调用 DeepSeek API
     */
    private String callDeepSeekApi(String prompt, int maxTokens) {
        String url = deepSeekConfig.getBaseUrl() + API_ENDPOINT;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepSeekConfig.getApiKey());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", deepSeekConfig.getModel());
        requestBody.put("temperature", deepSeekConfig.getTemperature());
        requestBody.put("max_tokens", maxTokens);

        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的知识管理助手，擅长提取文章核心观点、金句和案例。请用中文回答，保持简洁专业。");

        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        HttpEntity<String> request;
        try {
            request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        } catch (Exception e) {
            throw new RuntimeException("构建请求失败", e);
        }

        int retries = 0;
        while (retries < deepSeekConfig.getMaxRetries()) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    JsonNode choices = root.path("choices");
                    if (choices.isArray() && choices.size() > 0) {
                        String content = choices.get(0).path("message").path("content").asText();
                        return content.trim();
                    }
                }
                throw new RuntimeException("API返回异常: " + response.getStatusCode());
                
            } catch (Exception e) {
                retries++;
                log.warn("DeepSeek API调用失败，重试 {}/{}", retries, deepSeekConfig.getMaxRetries(), e);
                if (retries >= deepSeekConfig.getMaxRetries()) {
                    throw new RuntimeException("DeepSeek API调用失败: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(1000 * retries);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("中断", ie);
                }
            }
        }
        
        throw new RuntimeException("DeepSeek API调用失败，已达到最大重试次数");
    }

    // ==================== Prompt Engineering ====================

    /**
     * 构建一句话导读 Prompt
     */
    private String buildOneSentenceSummaryPrompt(String content) {
        return String.format("""
            请用一句话（不超过50字）概括以下文章的核心观点，要求：
            1. 简洁有力，直击要害
            2. 突出文章最独特的见解
            3. 引发读者兴趣
            
            文章内容：
            %s
            
            只返回一句话导读，不要有任何其他内容。""",
            truncateContent(content, 3000)
        );
    }

    /**
     * 构建摘要 Prompt
     */
    private String buildSummaryPrompt(String content) {
        return String.format("""
            请对以下文章进行结构化摘要，提取3-5个核心观点：
            
            要求：
            1. 每个观点用一句话概括
            2. 观点之间要有逻辑层次
            3. 保留原文的关键数据和结论
            4. 总字数控制在300-500字
            
            输出格式：
            【核心观点1】...
            【核心观点2】...
            ...
            
            文章内容：
            %s""",
            truncateContent(content, 4000)
        );
    }

    /**
     * 构建关键词提取 Prompt
     */
    private String buildKeywordExtractionPrompt(String content) {
        return String.format("""
            请从以下文章中提取5-8个关键词/标签，要求：
            1. 覆盖文章的核心主题
            2. 包含领域专业术语
            3. 便于后续检索和分类
            
            输出格式：用逗号分隔的关键词列表，如：认知科学,学习方法,大脑可塑性,记忆宫殿
            
            文章内容：
            %s
            
            关键词：""",
            truncateContent(content, 2000)
        );
    }

    /**
     * 构建金句提取 Prompt
     */
    private String buildGoldenQuotePrompt(String content) {
        return String.format("""
            请从以下文章中提取3-5个最有价值的金句/名言，要求：
            1. 原文摘录，保持完整
            2. 具有启发性或洞见性
            3. 可以独立成义
            
            输出格式为JSON数组：
            [
              {
                "content": "金句原文",
                "explanation": "简要解释这句话的意义"
              }
            ]
            
            文章内容：
            %s
            
            JSON输出：""",
            truncateContent(content, 4000)
        );
    }

    /**
     * 构建案例提取 Prompt
     */
    private String buildCaseExtractionPrompt(String content) {
        return String.format("""
            请从以下文章中提取2-4个典型案例/故事，要求：
            1. 案例要有具体情境
            2. 说明案例要论证的观点
            3. 案例类型可以是个人经历、商业案例、历史事件、科学研究等
            
            输出格式为JSON数组：
            [
              {
                "title": "案例标题",
                "content": "案例内容简述",
                "caseType": "personal/business/historical/scientific",
                "relatedPoint": "关联的核心观点"
              }
            ]
            
            文章内容：
            %s
            
            JSON输出：""",
            truncateContent(content, 4000)
        );
    }

    /**
     * 构建脑图生成 Prompt
     */
    private String buildMindMapPrompt(String summaryText, String keyPoints) {
        return String.format("""
            请根据以下内容生成一个脑图结构（思维导图），要求：
            1. 中央主题是文章核心观点
            2. 分3-5个主要分支
            3. 每个分支下有2-4个子节点
            4. 节点内容简洁，不超过15字
            
            输出格式为JSON：
            {
              "id": "root",
              "text": "中心主题",
              "children": [
                {
                  "id": "node1",
                  "text": "分支1",
                  "children": [
                    {"id": "node1-1", "text": "子节点1"}
                  ]
                }
              ]
            }
            
            内容摘要：
            %s
            
            核心观点：
            %s
            
            JSON输出：""",
            summaryText, keyPoints
        );
    }

    // ==================== 辅助方法 ====================

    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    private int calculateReadingTime(String content) {
        // 中文阅读速度约300字/分钟，英文约200词/分钟
        int chineseChars = countChineseChars(content);
        int englishWords = countEnglishWords(content);
        int minutes = (chineseChars / 300) + (englishWords / 200);
        return Math.max(1, minutes);
    }

    private int countChineseChars(String content) {
        int count = 0;
        for (char c : content.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') {
                count++;
            }
        }
        return count;
    }

    private int countEnglishWords(String content) {
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int estimateTokenCount(String content) {
        // 粗略估算：中文字符 + 英文单词*0.25
        return countChineseChars(content) + (int)(countEnglishWords(content) * 0.25);
    }

    private List<String> parseKeywords(String keywordsStr) {
        List<String> keywords = new ArrayList<>();
        if (keywordsStr != null && !keywordsStr.isEmpty()) {
            String[] parts = keywordsStr.split("[,，、]");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    keywords.add(trimmed);
                }
            }
        }
        return keywords;
    }

    private List<AiGeneratedContentResponse.GoldenQuote> parseGoldenQuotes(String json) {
        List<AiGeneratedContentResponse.GoldenQuote> quotes = new ArrayList<>();
        try {
            String extractedJson = extractJsonFromResponse(json);
            JsonNode array = objectMapper.readTree(extractedJson);
            if (array.isArray()) {
                for (JsonNode node : array) {
                    quotes.add(AiGeneratedContentResponse.GoldenQuote.builder()
                            .content(node.path("content").asText())
                            .explanation(node.path("explanation").asText())
                            .paragraphIndex(node.path("paragraphIndex").asInt(-1))
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("解析金句JSON失败，返回空列表", e);
        }
        return quotes;
    }

    private List<AiGeneratedContentResponse.ExtractedCase> parseCases(String json) {
        List<AiGeneratedContentResponse.ExtractedCase> cases = new ArrayList<>();
        try {
            String extractedJson = extractJsonFromResponse(json);
            JsonNode array = objectMapper.readTree(extractedJson);
            if (array.isArray()) {
                for (JsonNode node : array) {
                    cases.add(AiGeneratedContentResponse.ExtractedCase.builder()
                            .title(node.path("title").asText())
                            .content(node.path("content").asText())
                            .caseType(node.path("caseType").asText("general"))
                            .relatedPoint(node.path("relatedPoint").asText())
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("解析案例JSON失败，返回空列表", e);
        }
        return cases;
    }

    private String extractJsonFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "{}";
        }
        
        // 尝试提取JSON代码块
        Pattern pattern = Pattern.compile("```json\\s*(\\{.*\\})\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 尝试提取方括号JSON数组
        pattern = Pattern.compile("```json\\s*(\\[.*\\])\\s*```", Pattern.DOTALL);
        matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 查找第一个 { 或 [
        int objectStart = response.indexOf('{');
        int arrayStart = response.indexOf('[');
        
        if (objectStart != -1 && (arrayStart == -1 || objectStart < arrayStart)) {
            // 找到匹配的 }
            int braceCount = 0;
            for (int i = objectStart; i < response.length(); i++) {
                if (response.charAt(i) == '{') braceCount++;
                else if (response.charAt(i) == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        return response.substring(objectStart, i + 1);
                    }
                }
            }
        } else if (arrayStart != -1) {
            // 找到匹配的 ]
            int bracketCount = 0;
            for (int i = arrayStart; i < response.length(); i++) {
                if (response.charAt(i) == '[') bracketCount++;
                else if (response.charAt(i) == ']') {
                    bracketCount--;
                    if (bracketCount == 0) {
                        return response.substring(arrayStart, i + 1);
                    }
                }
            }
        }
        
        return response.trim();
    }
}
