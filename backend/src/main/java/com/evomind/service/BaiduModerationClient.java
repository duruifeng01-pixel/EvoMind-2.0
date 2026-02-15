package com.evomind.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * 百度内容审核API客户端
 * 文档: https://ai.baidu.com/ai-doc/ANTIPORN/Nk3h6xbb2
 */
@Slf4j
@Component
public class BaiduModerationClient {

    @Value("${baidu.moderation.app-id:}")
    private String appId;

    @Value("${baidu.moderation.api-key:}")
    private String apiKey;

    @Value("${baidu.moderation.secret-key:}")
    private String secretKey;

    @Value("${baidu.moderation.enabled:false}")
    private boolean enabled;

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String MODERATION_URL = "https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private long tokenExpireTime;

    public BaiduModerationClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 审核文本内容
     * 
     * @param text 待审核文本
     * @return 审核结果
     */
    public ModerationResult moderateText(String text) {
        if (!enabled) {
            log.debug("Baidu moderation is disabled, skipping");
            return ModerationResult.passed(null);
        }

        if (text == null || text.trim().isEmpty()) {
            return ModerationResult.passed(null);
        }

        try {
            String token = getAccessToken();
            String url = MODERATION_URL + "?access_token=" + token;

            // 构建请求体
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String requestBody = "text=" + encodedText;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseResponse(response.getBody());
            } else {
                log.error("Baidu moderation API error: HTTP {}", response.getStatusCode());
                return ModerationResult.error("API返回非200状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Baidu moderation error: {}", e.getMessage(), e);
            return ModerationResult.error(e.getMessage());
        }
    }

    /**
     * 获取访问令牌（带缓存）
     */
    private synchronized String getAccessToken() {
        // 检查token是否过期（提前5分钟刷新）
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime - 300000) {
            return accessToken;
        }

        try {
            String url = TOKEN_URL + "?grant_type=client_credentials&client_id=" + 
                        apiKey + "&client_secret=" + secretKey;

            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                if (jsonNode.has("access_token")) {
                    accessToken = jsonNode.get("access_token").asText();
                    long expiresIn = jsonNode.get("expires_in").asLong();
                    tokenExpireTime = System.currentTimeMillis() + (expiresIn * 1000);
                    
                    log.debug("Baidu access token refreshed, expires in {} seconds", expiresIn);
                    return accessToken;
                } else {
                    throw new RuntimeException("Failed to get access token: " + response.getBody());
                }
            } else {
                throw new RuntimeException("Token request failed: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to get Baidu access token: {}", e.getMessage(), e);
            throw new RuntimeException("获取百度访问令牌失败", e);
        }
    }

    /**
     * 解析审核响应
     */
    private ModerationResult parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            ModerationResult result = new ModerationResult();
            result.setRequestId(UUID.randomUUID().toString());
            result.setRawResponse(responseBody);
            result.setSuccess(true);

            // 检查错误码
            if (root.has("error_code")) {
                int errorCode = root.get("error_code").asInt();
                if (errorCode != 0) {
                    result.setSuccess(false);
                    result.setErrorMsg(root.get("error_msg").asText());
                    return result;
                }
            }

            // 解析结论
            // conclusion: 1-合规, 2-不合规, 3-疑似, 4-审核失败
            if (root.has("conclusion")) {
                String conclusion = root.get("conclusion").asText();
                result.setConclusion(conclusion);
                
                switch (conclusion) {
                    case "合规":
                        result.setConclusionPass(true);
                        break;
                    case "不合规":
                        result.setConclusionPass(false);
                        // 解析违规类型
                        if (root.has("data")) {
                            JsonNode dataArray = root.get("data");
                            if (dataArray.isArray() && dataArray.size() > 0) {
                                JsonNode firstHit = dataArray.get(0);
                                result.setViolationType(firstHit.get("subType").asText());
                                result.setViolationDesc(buildViolationDesc(dataArray));
                            }
                        }
                        break;
                    case "疑似":
                        result.setConclusionPass(false);
                        result.setViolationType("SUSPECTED");
                        result.setViolationDesc("内容疑似违规，建议人工复核");
                        break;
                    case "审核失败":
                        result.setSuccess(false);
                        result.setErrorMsg("审核失败，请重试");
                        break;
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to parse moderation response: {}", e.getMessage());
            return ModerationResult.error("解析响应失败: " + e.getMessage());
        }
    }

    /**
     * 构建违规描述
     */
    private String buildViolationDesc(JsonNode dataArray) {
        StringBuilder desc = new StringBuilder("命中违规类型: ");
        
        for (int i = 0; i < Math.min(dataArray.size(), 3); i++) {
            JsonNode item = dataArray.get(i);
            if (i > 0) desc.append("; ");
            desc.append(item.get("msg").asText());
            if (item.has("hits") && item.get("hits").size() > 0) {
                JsonNode hits = item.get("hits");
                desc.append("(");
                for (int j = 0; j < Math.min(hits.size(), 2); j++) {
                    if (j > 0) desc.append(", ");
                    if (hits.get(j).has("words")) {
                        desc.append(hits.get(j).get("words").asText());
                    }
                }
                desc.append(")");
            }
        }
        
        return desc.toString();
    }

    /**
     * 审核结果
     */
    @Data
    public static class ModerationResult {
        private boolean success;
        private String requestId;
        private boolean conclusionPass;
        private String conclusion;
        private String violationType;
        private String violationDesc;
        private String errorMsg;
        private String rawResponse;

        public boolean isConclusionPass() {
            return success && conclusionPass;
        }

        public static ModerationResult passed(String requestId) {
            ModerationResult result = new ModerationResult();
            result.setSuccess(true);
            result.setRequestId(requestId);
            result.setConclusionPass(true);
            result.setConclusion("合规");
            return result;
        }

        public static ModerationResult error(String errorMsg) {
            ModerationResult result = new ModerationResult();
            result.setSuccess(false);
            result.setErrorMsg(errorMsg);
            return result;
        }
    }
}