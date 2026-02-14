package com.evomind.service.impl;

import com.evomind.service.VoiceTranscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 语音转文字服务实现
 * 集成百度语音识别API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceTranscriptionServiceImpl implements VoiceTranscriptionService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${baidu.speech.app-id:}")
    private String appId;

    @Value("${baidu.speech.api-key:}")
    private String apiKey;

    @Value("${baidu.speech.secret-key:}")
    private String secretKey;

    @Value("${baidu.speech.enabled:false}")
    private Boolean speechEnabled;

    // 百度语音识别API
    private static final String ASR_URL = "https://vop.baidu.com/server_api";
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";

    // 缓存的token
    private volatile String cachedToken = null;
    private volatile long tokenExpireTime = 0;

    @Override
    public String transcribeSync(MultipartFile audioFile) {
        return transcribeSync(audioFile, "pcm", 16000);
    }

    @Override
    public String transcribeSync(MultipartFile audioFile, String format, int sampleRate) {
        if (Boolean.FALSE.equals(speechEnabled) || apiKey.isEmpty()) {
            log.info("语音服务未启用或API密钥为空，返回模拟转写结果");
            return mockTranscribe();
        }

        try {
            byte[] audioData = audioFile.getBytes();
            String audioBase64 = Base64Utils.encodeToString(audioData);
            return transcribeFromBase64(audioBase64, format, sampleRate);
        } catch (IOException e) {
            log.error("读取音频文件失败", e);
            throw new RuntimeException("读取音频文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String transcribeFromBase64(String audioBase64, String format, int sampleRate) {
        if (Boolean.FALSE.equals(speechEnabled) || apiKey.isEmpty()) {
            log.info("语音服务未启用或API密钥为空，返回模拟转写结果");
            return mockTranscribe();
        }

        try {
            String token = getAccessToken();
            String url = ASR_URL + "?cuid=" + appId + "&token=" + token;

            // 构建请求体
            String requestBody = buildRequestBody(audioBase64, format, sampleRate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            log.info("开始语音转写，格式={}, 采样率={}", format, sampleRate);
            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            long duration = System.currentTimeMillis() - startTime;
            log.info("语音转写完成，耗时{}ms", duration);

            return parseTranscriptionResult(response.getBody());

        } catch (Exception e) {
            log.error("语音转写失败", e);
            throw new RuntimeException("语音转写失败: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized String getAccessToken() {
        // 检查缓存的token是否有效
        if (cachedToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedToken;
        }

        try {
            String url = TOKEN_URL + "?grant_type=client_credentials&client_id=" 
                    + apiKey + "&client_secret=" + secretKey;

            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.has("access_token")) {
                cachedToken = jsonNode.get("access_token").asText();
                // token有效期通常为30天，这里缓存29天
                long expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asLong() : 2592000;
                tokenExpireTime = System.currentTimeMillis() + (expiresIn - 86400) * 1000;
                log.info("获取百度语音API Token成功");
                return cachedToken;
            } else {
                String error = jsonNode.has("error_description") 
                    ? jsonNode.get("error_description").asText() 
                    : "未知错误";
                throw new RuntimeException("获取Token失败: " + error);
            }
        } catch (Exception e) {
            log.error("获取百度语音API Token失败", e);
            throw new RuntimeException("获取Token失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return Boolean.TRUE.equals(speechEnabled) && !apiKey.isEmpty();
    }

    /**
     * 构建语音识别请求体
     */
    private String buildRequestBody(String audioBase64, String format, int sampleRate) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("format", format);
            root.put("rate", sampleRate);
            root.put("channel", 1);
            root.put("cuid", appId.isEmpty() ? "evomind_default" : appId);
            root.put("token", getAccessToken());
            root.put("speech", audioBase64);
            root.put("len", audioBase64.length() * 3 / 4); // Base64编码后长度的3/4约等于原长度

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("构建请求体失败", e);
        }
    }

    /**
     * 解析转写结果
     */
    private String parseTranscriptionResult(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            int errNo = root.has("err_no") ? root.get("err_no").asInt() : -1;
            if (errNo != 0) {
                String errMsg = root.has("err_msg") ? root.get("err_msg").asText() : "未知错误";
                throw new RuntimeException("语音识别失败: " + errMsg);
            }

            StringBuilder result = new StringBuilder();
            if (root.has("result")) {
                JsonNode resultArray = root.get("result");
                if (resultArray.isArray()) {
                    for (JsonNode item : resultArray) {
                        result.append(item.asText());
                    }
                }
            }

            return result.toString().trim();
        } catch (Exception e) {
            log.error("解析转写结果失败: {}", responseBody, e);
            throw new RuntimeException("解析转写结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 模拟转写（用于测试或API不可用时）
     */
    private String mockTranscribe() {
        String[] mockResults = {
            "这是一个灵感记录，关于今天的会议内容。",
            "明天需要完成项目文档的编写，记得跟进客户需求。",
            "阅读笔记：深度工作的核心是专注，减少干扰。",
            "产品想法：做一个AI助手，帮助整理碎片化信息。",
            "会议要点：Q4目标增长30%，需要增加营销投入。"
        };
        int index = (int) (System.currentTimeMillis() % mockResults.length);
        return mockResults[index];
    }
}
