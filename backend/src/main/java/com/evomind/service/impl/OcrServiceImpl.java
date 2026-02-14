package com.evomind.service.impl;

import com.evomind.dto.request.OcrImportRequest;
import com.evomind.dto.response.OcrResultResponse;
import com.evomind.entity.OcrImportLog;
import com.evomind.repository.OcrImportLogRepository;
import com.evomind.repository.SourceRepository;
import com.evomind.service.OcrService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR服务实现
 * 集成百度OCR API实现图片文字识别
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrServiceImpl implements OcrService {

    private final OcrImportLogRepository ocrImportLogRepository;
    private final SourceRepository sourceRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${baidu.ocr.app-id:}")
    private String appId;

    @Value("${baidu.ocr.api-key:}")
    private String apiKey;

    @Value("${baidu.ocr.secret-key:}")
    private String secretKey;

    @Value("${baidu.ocr.enabled:false}")
    private Boolean ocrEnabled;

    private static final String BAIDU_OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic";
    private static final String BAIDU_OCR_HIGH_PRECISION_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate";
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";

    // 小红书博主名称模式
    private static final Pattern XIAOHONGSHU_PATTERN = Pattern.compile("@([\\u4e00-\\u9fa5\\w]{2,20})|([\\u4e00-\\u9fa5\\w]{2,20})\\s*[·|]\\s*小红书");
    // 微信公众号模式
    private static final Pattern WECHAT_PATTERN = Pattern.compile("公众号[：:]\\s*([\\u4e00-\\u9fa5\\w]{2,30})|([\\u4e00-\\u9fa5\\w]{2,30})\\s*\\(微信号");
    // 抖音号模式
    private static final Pattern DOUYIN_PATTERN = Pattern.compile("抖音号[：:]\\s*(@?[\\w.]{2,24})|@([\\u4e00-\\u9fa5\\w]{2,20})");

    @Override
    @Transactional
    public OcrResultResponse recognizeBloggers(Long userId, OcrImportRequest request) {
        long startTime = System.currentTimeMillis();
        String taskId = generateTaskId();

        try {
            // 创建处理中日志
            OcrImportLog importLog = new OcrImportLog();
            importLog.setUserId(userId);
            importLog.setTaskId(taskId);
            importLog.setPlatform(request.getPlatform());
            importLog.setStatus(OcrImportLog.OcrStatus.PROCESSING);
            ocrImportLogRepository.save(importLog);

            // 提取图片数据
            String imageBase64 = extractBase64FromDataUrl(request.getImageData());
            String imageHash = calculateImageHash(imageBase64);
            importLog.setImageHash(imageHash);

            // 检查是否重复识别
            Optional<OcrImportLog> existingLog = ocrImportLogRepository.findByUserIdAndImageHash(userId, imageHash);
            if (existingLog.isPresent() && existingLog.get().getStatus() == OcrImportLog.OcrStatus.SUCCESS) {
                log.info("图片已识别过，返回缓存结果: taskId={}", existingLog.get().getTaskId());
                return getResultByTaskId(existingLog.get().getTaskId());
            }

            // 执行OCR识别
            OcrResultResponse result;
            if (Boolean.FALSE.equals(ocrEnabled) || apiKey.isEmpty()) {
                // 模拟模式：使用模拟数据
                result = mockRecognize(imageBase64, request.getPlatform());
            } else {
                // 真实模式：调用百度OCR API
                result = callBaiduOcr(imageBase64, request.getPlatform());
            }

            // 设置任务ID和时间戳
            result.setTaskId(taskId);
            result.setRecognizedAt(LocalDateTime.now());
            long processingTime = System.currentTimeMillis() - startTime;
            result.setProcessingTimeMs(processingTime);

            // 检查用户已有源
            result.getBloggers().forEach(blogger -> {
                sourceRepository.findByUserIdAndName(userId, blogger.getName())
                    .ifPresent(source -> {
                        blogger.setAlreadyExists(true);
                        blogger.setExistingSourceId(source.getId());
                    });
            });

            // 更新日志
            importLog.setStatus(OcrImportLog.OcrStatus.SUCCESS);
            importLog.setDetectedCount(result.getBloggers().size());
            importLog.setProcessingTimeMs(processingTime);
            importLog.setRawResult(objectMapper.writeValueAsString(result));
            ocrImportLogRepository.save(importLog);

            log.info("OCR识别完成: userId={}, taskId={}, 识别到{}个博主, 耗时{}ms",
                userId, taskId, result.getBloggers().size(), processingTime);

            return result;

        } catch (Exception e) {
            log.error("OCR识别失败", e);

            // 更新失败日志
            ocrImportLogRepository.findByTaskId(taskId).ifPresent(importLog -> {
                importLog.setStatus(OcrImportLog.OcrStatus.FAILED);
                importLog.setErrorMessage(e.getMessage());
                ocrImportLogRepository.save(importLog);
            });

            throw new RuntimeException("OCR识别失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String recognizeText(String imageBase64, boolean detectDirection) {
        if (Boolean.FALSE.equals(ocrEnabled) || apiKey.isEmpty()) {
            return mockRecognizeText();
        }

        try {
            String accessToken = getAccessToken();
            String url = BAIDU_OCR_URL + "?access_token=" + accessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "image=" + URLEncoder.encode(imageBase64, StandardCharsets.UTF_8)
                + "&detect_direction=" + detectDirection;

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            StringBuilder result = new StringBuilder();

            if (root.has("words_result")) {
                ArrayNode wordsResult = (ArrayNode) root.get("words_result");
                for (JsonNode word : wordsResult) {
                    result.append(word.get("words").asText()).append("\n");
                }
            }

            return result.toString();
        } catch (Exception e) {
            log.error("文字识别失败", e);
            throw new RuntimeException("文字识别失败", e);
        }
    }

    @Override
    public OcrResultResponse recognizeTextWithLocation(String imageBase64) {
        if (Boolean.FALSE.equals(ocrEnabled) || apiKey.isEmpty()) {
            return mockRecognize(imageBase64, "other");
        }

        try {
            String accessToken = getAccessToken();
            String url = BAIDU_OCR_HIGH_PRECISION_URL + "?access_token=" + accessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "image=" + URLEncoder.encode(imageBase64, StandardCharsets.UTF_8)
                + "&vertexes_location=true&probability=true";

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            return parseBaiduOcrResult(response.getBody());
        } catch (Exception e) {
            log.error("高精度文字识别失败", e);
            throw new RuntimeException("高精度文字识别失败", e);
        }
    }

    @Override
    public OcrResultResponse getResultByTaskId(String taskId) {
        OcrImportLog log = ocrImportLogRepository.findByTaskId(taskId)
            .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));

        try {
            return objectMapper.readValue(log.getRawResult(), OcrResultResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("解析结果失败", e);
        }
    }

    @Override
    public OcrResultResponse parseXiaohongshuScreenshot(String imageBase64) {
        OcrImportRequest request = new OcrImportRequest();
        request.setImageData(imageBase64);
        request.setPlatform("xiaohongshu");
        return recognizeBloggers(0L, request); // 0L表示临时识别，不保存到用户
    }

    @Override
    public OcrResultResponse parseWechatScreenshot(String imageBase64) {
        OcrImportRequest request = new OcrImportRequest();
        request.setImageData(imageBase64);
        request.setPlatform("weixin");
        return recognizeBloggers(0L, request);
    }

    /**
     * 调用百度OCR API
     */
    private OcrResultResponse callBaiduOcr(String imageBase64, String platform) throws Exception {
        String accessToken = getAccessToken();
        String url = BAIDU_OCR_HIGH_PRECISION_URL + "?access_token=" + accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "image=" + URLEncoder.encode(imageBase64, StandardCharsets.UTF_8)
            + "&vertexes_location=true&probability=true&language_type=CHN_ENG";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return parseBaiduOcrResult(response.getBody());
    }

    /**
     * 解析百度OCR返回结果
     */
    private OcrResultResponse parseBaiduOcrResult(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);
        List<OcrResultResponse.TextBlock> textBlocks = new ArrayList<>();
        List<OcrResultResponse.DetectedBlogger> bloggers = new ArrayList<>();

        if (root.has("words_result")) {
            ArrayNode wordsResult = (ArrayNode) root.get("words_result");

            for (int i = 0; i < wordsResult.size(); i++) {
                JsonNode word = wordsResult.get(i);
                String text = word.get("words").asText();

                // 创建文本块
                OcrResultResponse.TextBlock.TextBlockBuilder blockBuilder = OcrResultResponse.TextBlock.builder()
                    .text(text);

                // 提取位置信息
                if (word.has("location")) {
                    JsonNode loc = word.get("location");
                    blockBuilder.boundingBox(OcrResultResponse.BoundingBox.builder()
                        .x(loc.get("left").asInt())
                        .y(loc.get("top").asInt())
                        .width(loc.get("width").asInt())
                        .height(loc.get("height").asInt())
                        .build());
                }

                // 提取置信度
                if (word.has("probability")) {
                    JsonNode prob = word.get("probability");
                    if (prob.has("average")) {
                        blockBuilder.confidence(prob.get("average").asDouble());
                    }
                }

                textBlocks.add(blockBuilder.build());

                // 识别博主名称
                Optional<OcrResultResponse.DetectedBlogger> blogger = extractBloggerFromText(text, i);
                blogger.ifPresent(bloggers::add);
            }
        }

        // 去重并过滤低置信度
        List<OcrResultResponse.DetectedBlogger> uniqueBloggers = deduplicateBloggers(bloggers);

        return OcrResultResponse.builder()
            .textBlocks(textBlocks)
            .bloggers(uniqueBloggers)
            .status("SUCCESS")
            .needsConfirmation(true)
            .build();
    }

    /**
     * 从文本中提取博主信息
     */
    private Optional<OcrResultResponse.DetectedBlogger> extractBloggerFromText(String text, int index) {
        // 小红书
        Matcher xhsMatcher = XIAOHONGSHU_PATTERN.matcher(text);
        if (xhsMatcher.find()) {
            String name = xhsMatcher.group(1) != null ? xhsMatcher.group(1) : xhsMatcher.group(2);
            return Optional.of(OcrResultResponse.DetectedBlogger.builder()
                .candidateId("cand_" + index)
                .name(name)
                .platform("xiaohongshu")
                .homeUrl("https://www.xiaohongshu.com/user/profile/" + name)
                .confidence(0.85)
                .alreadyExists(false)
                .build());
        }

        // 微信公众号
        Matcher wxMatcher = WECHAT_PATTERN.matcher(text);
        if (wxMatcher.find()) {
            String name = wxMatcher.group(1) != null ? wxMatcher.group(1) : wxMatcher.group(2);
            return Optional.of(OcrResultResponse.DetectedBlogger.builder()
                .candidateId("cand_" + index)
                .name(name)
                .platform("weixin")
                .homeUrl("https://mp.weixin.qq.com")
                .confidence(0.82)
                .alreadyExists(false)
                .build());
        }

        // 抖音
        Matcher dyMatcher = DOUYIN_PATTERN.matcher(text);
        if (dyMatcher.find()) {
            String name = dyMatcher.group(1) != null ? dyMatcher.group(1) : dyMatcher.group(2);
            return Optional.of(OcrResultResponse.DetectedBlogger.builder()
                .candidateId("cand_" + index)
                .name(name)
                .platform("douyin")
                .homeUrl("https://www.douyin.com/user/" + name)
                .confidence(0.80)
                .alreadyExists(false)
                .build());
        }

        // 通用博主识别：@开头的名称
        if (text.startsWith("@") && text.length() > 2 && text.length() < 25) {
            String name = text.substring(1).trim();
            return Optional.of(OcrResultResponse.DetectedBlogger.builder()
                .candidateId("cand_" + index)
                .name(name)
                .platform("other")
                .confidence(0.70)
                .alreadyExists(false)
                .build());
        }

        return Optional.empty();
    }

    /**
     * 去重博主列表
     */
    private List<OcrResultResponse.DetectedBlogger> deduplicateBloggers(List<OcrResultResponse.DetectedBlogger> bloggers) {
        Map<String, OcrResultResponse.DetectedBlogger> uniqueMap = new LinkedHashMap<>();

        for (OcrResultResponse.DetectedBlogger blogger : bloggers) {
            String key = blogger.getName().toLowerCase();
            if (!uniqueMap.containsKey(key) || blogger.getConfidence() > uniqueMap.get(key).getConfidence()) {
                uniqueMap.put(key, blogger);
            }
        }

        return new ArrayList<>(uniqueMap.values());
    }

    /**
     * 获取百度API访问令牌
     */
    private String getAccessToken() throws Exception {
        String url = TOKEN_URL + "?grant_type=client_credentials&client_id="
            + apiKey + "&client_secret=" + secretKey;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());

        if (root.has("access_token")) {
            return root.get("access_token").asText();
        }

        throw new RuntimeException("获取AccessToken失败: " + response.getBody());
    }

    /**
     * 从Data URL提取Base64数据
     */
    private String extractBase64FromDataUrl(String dataUrl) {
        if (dataUrl == null || dataUrl.isEmpty()) {
            throw new IllegalArgumentException("图片数据不能为空");
        }

        if (dataUrl.startsWith("data:")) {
            int commaIndex = dataUrl.indexOf(",");
            if (commaIndex > 0) {
                return dataUrl.substring(commaIndex + 1);
            }
        }

        // 假设纯Base64
        return dataUrl;
    }

    /**
     * 计算图片Hash（用于去重）
     */
    private String calculateImageHash(String base64) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(base64.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * 生成任务ID
     */
    private String generateTaskId() {
        return "ocr_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 模拟识别（用于测试）
     */
    private OcrResultResponse mockRecognize(String imageBase64, String platform) {
        List<OcrResultResponse.DetectedBlogger> bloggers = new ArrayList<>();

        if ("xiaohongshu".equals(platform)) {
            bloggers.add(createMockBlogger("cand_1", "知识管理达人", "xiaohongshu", 0.92));
            bloggers.add(createMockBlogger("cand_2", "效率工具控", "xiaohongshu", 0.88));
            bloggers.add(createMockBlogger("cand_3", "认知提升笔记", "xiaohongshu", 0.85));
            bloggers.add(createMockBlogger("cand_4", "深度思考者", "xiaohongshu", 0.81));
            bloggers.add(createMockBlogger("cand_5", "读书笔记分享", "xiaohongshu", 0.78));
        } else if ("weixin".equals(platform)) {
            bloggers.add(createMockBlogger("cand_1", "孤独大脑", "weixin", 0.90));
            bloggers.add(createMockBlogger("cand_2", "辉哥奇谭", "weixin", 0.87));
            bloggers.add(createMockBlogger("cand_3", "刘润", "weixin", 0.86));
        } else {
            bloggers.add(createMockBlogger("cand_1", "科技博主小明", "other", 0.85));
            bloggers.add(createMockBlogger("cand_2", "产品思维笔记", "other", 0.82));
            bloggers.add(createMockBlogger("cand_3", "运营增长黑客", "other", 0.79));
        }

        return OcrResultResponse.builder()
            .bloggers(bloggers)
            .status("SUCCESS")
            .needsConfirmation(true)
            .build();
    }

    private OcrResultResponse.DetectedBlogger createMockBlogger(String id, String name, String platform, double confidence) {
        String homeUrl = switch (platform) {
            case "xiaohongshu" -> "https://www.xiaohongshu.com/user/profile/" + name;
            case "weixin" -> "https://mp.weixin.qq.com";
            case "douyin" -> "https://www.douyin.com/user/" + name;
            default -> "";
        };

        return OcrResultResponse.DetectedBlogger.builder()
            .candidateId(id)
            .name(name)
            .platform(platform)
            .homeUrl(homeUrl)
            .confidence(confidence)
            .alreadyExists(false)
            .build();
    }

    private String mockRecognizeText() {
        return "小红书发现页\n@知识管理达人\n分享高效学习方法\n\n@效率工具控\n推荐实用App\n\n关注";
    }
}
