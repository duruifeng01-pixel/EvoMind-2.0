package com.evomind.service.impl;

import com.evomind.dto.request.ModerationRequest;
import com.evomind.dto.response.ModerationResponse;
import com.evomind.entity.ContentModerationLog;
import com.evomind.entity.SensitiveWord;
import com.evomind.repository.ContentModerationLogRepository;
import com.evomind.service.BaiduModerationClient;
import com.evomind.service.ContentModerationService;
import com.evomind.service.SensitiveWordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 内容审核服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentModerationServiceImpl implements ContentModerationService {

    private final ContentModerationLogRepository moderationLogRepository;
    private final SensitiveWordService sensitiveWordService;
    private final BaiduModerationClient baiduModerationClient;
    private final ObjectMapper objectMapper;

    // 内容摘要长度限制
    private static final int SUMMARY_LENGTH = 200;

    @Override
    @Transactional
    public ModerationResponse moderateContent(Long userId, ModerationRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 检查是否有缓存结果
        if (!request.getForceReCheck() && request.getContentId() != null) {
            ModerationResponse cached = getCachedResult(request.getContentId(), request.getContentType());
            if (cached != null) {
                log.debug("Using cached moderation result for content: {}", request.getContentId());
                return cached;
            }
        }
        
        // 创建审核日志记录
        ContentModerationLog log = createModerationLog(userId, request);
        
        try {
            // Step 1: 敏感词检测
            List<SensitiveWordService.SensitiveWordHitResult> sensitiveHits = 
                    sensitiveWordService.findSensitiveWords(request.getContent());
            
            if (!sensitiveHits.isEmpty()) {
                // 敏感词命中，记录并返回
                return handleSensitiveWordHit(log, sensitiveHits, startTime);
            }
            
            // Step 2: 调用百度内容审核API（对于高风险内容）
            if (shouldUseThirdPartyApi(request)) {
                BaiduModerationClient.ModerationResult apiResult = 
                        baiduModerationClient.moderateText(request.getContent());
                
                return handleApiResult(log, apiResult, startTime);
            }
            
            // Step 3: 本地审核通过
            return approveContent(log, ContentModerationLog.ModerationType.AUTO_SENSITIVE_WORD, startTime);
            
        } catch (Exception e) {
            log.error("Moderation error for user {}: {}", userId, e.getMessage(), e);
            return handleError(log, e, startTime);
        }
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<ModerationResponse> moderateContentAsync(Long userId, ModerationRequest request) {
        return CompletableFuture.completedFuture(moderateContent(userId, request));
    }

    @Override
    public boolean quickCheck(String content) {
        return sensitiveWordService.containsSensitiveWord(content);
    }

    @Override
    public List<ModerationResponse.HitWordInfo> quickCheckWithDetails(String content) {
        List<SensitiveWordService.SensitiveWordHitResult> hits = sensitiveWordService.findSensitiveWords(content);
        List<ModerationResponse.HitWordInfo> result = new ArrayList<>();
        
        for (SensitiveWordService.SensitiveWordHitResult hit : hits) {
            List<ModerationResponse.HitWordInfo.Position> positions = new ArrayList<>();
            for (SensitiveWordService.Position pos : hit.positions) {
                positions.add(ModerationResponse.HitWordInfo.Position.builder()
                        .start(pos.start)
                        .end(pos.end)
                        .build());
            }
            
            result.add(ModerationResponse.HitWordInfo.builder()
                    .wordId(hit.wordId)
                    .word(hit.word)
                    .category(hit.category.getLabel())
                    .level(hit.level.getLabel())
                    .positions(positions)
                    .build());
        }
        
        return result;
    }

    @Override
    @Transactional
    public ModerationResponse moderateAiGeneratedContent(Long userId, String content, 
                                                          ContentModerationLog.ContentType contentType,
                                                          String aiModel) {
        ModerationRequest request = new ModerationRequest();
        request.setContentType(contentType);
        request.setContent(content);
        request.setIsAiGenerated(true);
        request.setAiModel(aiModel);
        
        return moderateContent(userId, request);
    }

    @Override
    @Transactional
    public ModerationResponse moderateUserContent(Long userId, String content,
                                                   ContentModerationLog.ContentType contentType) {
        ModerationRequest request = new ModerationRequest();
        request.setContentType(contentType);
        request.setContent(content);
        request.setIsAiGenerated(false);
        
        return moderateContent(userId, request);
    }

    @Override
    public ContentModerationLog getModerationLog(Long logId) {
        return moderationLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("审核记录不存在: " + logId));
    }

    @Override
    public ModerationResponse getModerationResult(String contentId, ContentModerationLog.ContentType contentType) {
        return moderationLogRepository.findByContentIdAndContentType(contentId, contentType)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    public Page<ContentModerationLog> getUserModerationHistory(Long userId, Pageable pageable) {
        return moderationLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional
    public ModerationResponse manualReview(Long logId, Long reviewerId, 
                                            ContentModerationLog.ModerationStatus result,
                                            String remark) {
        ContentModerationLog log = getModerationLog(logId);
        
        log.setManualReviewRequired(false);
        log.setManualReviewResult(result);
        log.setReviewerId(reviewerId);
        log.setReviewedAt(LocalDateTime.now());
        log.setReviewRemark(remark);
        
        // 根据人工复核结果更新状态
        if (result == ContentModerationLog.ModerationStatus.APPROVED) {
            log.setModerationStatus(ContentModerationLog.ModerationStatus.APPROVED);
        } else if (result == ContentModerationLog.ModerationStatus.REJECTED) {
            log.setModerationStatus(ContentModerationLog.ModerationStatus.REJECTED);
        }
        
        moderationLogRepository.save(log);
        
        return convertToResponse(log);
    }

    @Override
    @Transactional
    public List<ModerationResponse> batchModerate(Long userId, List<ModerationRequest> requests) {
        List<ModerationResponse> results = new ArrayList<>();
        
        for (ModerationRequest request : requests) {
            try {
                results.add(moderateContent(userId, request));
            } catch (Exception e) {
                log.error("Batch moderation error for user {}: {}", userId, e.getMessage());
                results.add(ModerationResponse.builder()
                        .status(ContentModerationLog.ModerationStatus.ERROR)
                        .statusDescription("审核异常: " + e.getMessage())
                        .approved(false)
                        .shouldBlock(true)
                        .suggestedAction(ModerationResponse.SuggestedAction.RETRY)
                        .build());
            }
        }
        
        return results;
    }

    @Override
    public ModerationStatistics getStatistics(Long userId) {
        List<Object[]> counts;
        if (userId != null) {
            counts = moderationLogRepository.countByUserIdGroupByStatus(userId);
        } else {
            // 全局统计（最近30天）
            LocalDateTime start = LocalDateTime.now().minusDays(30);
            LocalDateTime end = LocalDateTime.now();
            counts = moderationLogRepository.countByStatusBetween(start, end);
        }
        
        ModerationStatistics stats = new ModerationStatistics();
        
        for (Object[] row : counts) {
            ContentModerationLog.ModerationStatus status = (ContentModerationLog.ModerationStatus) row[0];
            Long count = (Long) row[1];
            
            stats.totalCount += count;
            
            switch (status) {
                case APPROVED -> stats.approvedCount += count;
                case REJECTED -> stats.rejectedCount += count;
                case PENDING, PROCESSING -> stats.pendingCount += count;
                case NEED_REVIEW -> stats.needReviewCount += count;
            }
        }
        
        if (stats.totalCount > 0) {
            stats.approvalRate = (double) stats.approvedCount / stats.totalCount * 100;
            stats.rejectionRate = (double) stats.rejectedCount / stats.totalCount * 100;
        }
        
        return stats;
    }

    @Override
    @Transactional
    public ModerationResponse reModerate(Long logId, Long reviewerId) {
        ContentModerationLog log = getModerationLog(logId);
        
        ModerationRequest request = new ModerationRequest();
        request.setContentType(log.getContentType());
        request.setContent(log.getOriginalContent());
        request.setIsAiGenerated(log.getIsAiGenerated());
        request.setForceReCheck(true);
        
        return moderateContent(log.getUserId(), request);
    }

    // ==================== 私有辅助方法 ====================

    private ContentModerationLog createModerationLog(Long userId, ModerationRequest request) {
        String summary = request.getContent();
        if (summary.length() > SUMMARY_LENGTH) {
            summary = summary.substring(0, SUMMARY_LENGTH) + "...";
        }
        
        ContentModerationLog log = ContentModerationLog.builder()
                .userId(userId)
                .contentType(request.getContentType())
                .contentId(request.getContentId())
                .originalContent(request.getContent())
                .contentSummary(request.getContentSummary() != null ? request.getContentSummary() : summary)
                .moderationStatus(ContentModerationLog.ModerationStatus.PROCESSING)
                .isAiGenerated(request.getIsAiGenerated())
                .aiModel(request.getAiModel())
                .retryCount(0)
                .build();
        
        return moderationLogRepository.save(log);
    }

    private ModerationResponse getCachedResult(String contentId, ContentModerationLog.ContentType contentType) {
        return moderationLogRepository.findByContentIdAndContentType(contentId, contentType)
                .filter(log -> log.getModerationStatus() != ContentModerationLog.ModerationStatus.ERROR)
                .filter(log -> log.getCreatedAt().isAfter(LocalDateTime.now().minusHours(24))) // 缓存24小时
                .map(this::convertToResponse)
                .orElse(null);
    }

    private boolean shouldUseThirdPartyApi(ModerationRequest request) {
        // 以下情况需要调用第三方API：
        // 1. 用户发布的内容（非AI生成）
        // 2. 高优先级审核
        // 3. 特定类型的内容
        if (request.getHighPriority() != null && request.getHighPriority()) {
            return true;
        }
        
        if (request.getIsAiGenerated() != null && !request.getIsAiGenerated()) {
            // 用户内容需要更严格的审核
            return true;
        }
        
        // 评论、聊天消息等UGC内容需要第三方审核
        return request.getContentType() == ContentModerationLog.ContentType.USER_COMMENT ||
               request.getContentType() == ContentModerationLog.ContentType.CHAT_MESSAGE ||
               request.getContentType() == ContentModerationLog.ContentType.USER_PROFILE;
    }

    private ModerationResponse handleSensitiveWordHit(ContentModerationLog log, 
                                                       List<SensitiveWordService.SensitiveWordHitResult> hits,
                                                       long startTime) {
        long processTime = System.currentTimeMillis() - startTime;
        
        // 构建命中信息JSON
        List<Map<String, Object>> hitWordsJson = new ArrayList<>();
        boolean needBlock = false;
        boolean needReview = false;
        
        for (SensitiveWordService.SensitiveWordHitResult hit : hits) {
            Map<String, Object> hitMap = new HashMap<>();
            hitMap.put("wordId", hit.wordId);
            hitMap.put("word", hit.word);
            hitMap.put("category", hit.category.name());
            hitMap.put("level", hit.level.name());
            hitWordsJson.add(hitMap);
            
            if (hit.level == SensitiveWord.SensitiveLevel.CRITICAL) {
                needBlock = true;
            } else if (hit.level == SensitiveWord.SensitiveLevel.HIGH) {
                needReview = true;
            }
        }
        
        try {
            log.setHitSensitiveWords(objectMapper.writeValueAsString(hitWordsJson));
        } catch (Exception e) {
            log.setHitSensitiveWords(hits.toString());
        }
        
        log.setModerationStatus(needBlock ? ContentModerationLog.ModerationStatus.REJECTED : 
                                (needReview ? ContentModerationLog.ModerationStatus.NEED_REVIEW : 
                                 ContentModerationLog.ModerationStatus.APPROVED));
        log.setModerationType(ContentModerationLog.ModerationType.AUTO_SENSITIVE_WORD);
        log.setViolationType(ContentModerationLog.ViolationType.SENSITIVE_WORD);
        log.setViolationDetails("命中敏感词: " + hits.stream().map(h -> h.word).limit(3).reduce((a, b) -> a + ", " + b).orElse(""));
        log.setProcessTimeMs(processTime);
        log.setManualReviewRequired(needReview);
        
        moderationLogRepository.save(log);
        
        return ModerationResponse.builder()
                .logId(log.getId())
                .status(log.getModerationStatus())
                .statusDescription(log.getStatusDescription())
                .approved(!needBlock)
                .shouldBlock(needBlock)
                .violationType(log.getViolationType())
                .violationDetails(log.getViolationDetails())
                .hitSensitiveWords(convertHitsToResponse(hits))
                .moderationType(log.getModerationType())
                .provider("本地敏感词库")
                .needManualReview(needReview)
                .suggestedAction(needBlock ? ModerationResponse.SuggestedAction.BLOCK : 
                               (needReview ? ModerationResponse.SuggestedAction.REVIEW : 
                                ModerationResponse.SuggestedAction.ALLOW))
                .moderatedAt(LocalDateTime.now())
                .processTimeMs(processTime)
                .build();
    }

    private ModerationResponse handleApiResult(ContentModerationLog log, 
                                                BaiduModerationClient.ModerationResult apiResult,
                                                long startTime) {
        long processTime = System.currentTimeMillis() - startTime;
        
        log.setProvider("百度AI");
        log.setProviderResponse(apiResult.getRawResponse());
        log.setRequestId(apiResult.getRequestId());
        log.setProcessTimeMs(processTime);
        
        if (apiResult.isSuccess()) {
            if (apiResult.isConclusionPass()) {
                // 审核通过
                return approveContent(log, ContentModerationLog.ModerationType.AUTO_BAIDU_API, startTime);
            } else {
                // 审核不通过
                log.setModerationStatus(ContentModerationLog.ModerationStatus.REJECTED);
                log.setModerationType(ContentModerationLog.ModerationType.AUTO_BAIDU_API);
                log.setViolationType(mapViolationType(apiResult.getViolationType()));
                log.setViolationDetails(apiResult.getViolationDesc());
                
                moderationLogRepository.save(log);
                
                return ModerationResponse.builder()
                        .logId(log.getId())
                        .status(ContentModerationLog.ModerationStatus.REJECTED)
                        .statusDescription("审核不通过")
                        .approved(false)
                        .shouldBlock(true)
                        .violationType(log.getViolationType())
                        .violationDetails(log.getViolationDetails())
                        .moderationType(ContentModerationLog.ModerationType.AUTO_BAIDU_API)
                        .provider("百度AI")
                        .suggestedAction(ModerationResponse.SuggestedAction.BLOCK)
                        .moderatedAt(LocalDateTime.now())
                        .processTimeMs(processTime)
                        .build();
            }
        } else {
            // API调用失败，降级到本地审核
            log.warn("Baidu API failed: {}, falling back to local check", apiResult.getErrorMsg());
            return approveContent(log, ContentModerationLog.ModerationType.AUTO_SENSITIVE_WORD, startTime);
        }
    }

    private ModerationResponse approveContent(ContentModerationLog log, 
                                               ContentModerationLog.ModerationType moderationType,
                                               long startTime) {
        long processTime = System.currentTimeMillis() - startTime;
        
        log.setModerationStatus(ContentModerationLog.ModerationStatus.APPROVED);
        log.setModerationType(moderationType);
        log.setViolationType(ContentModerationLog.ViolationType.NONE);
        log.setProcessTimeMs(processTime);
        
        moderationLogRepository.save(log);
        
        return ModerationResponse.builder()
                .logId(log.getId())
                .status(ContentModerationLog.ModerationStatus.APPROVED)
                .statusDescription("审核通过")
                .approved(true)
                .shouldBlock(false)
                .moderationType(moderationType)
                .provider(moderationType == ContentModerationLog.ModerationType.AUTO_BAIDU_API ? "百度AI" : "本地审核")
                .suggestedAction(ModerationResponse.SuggestedAction.ALLOW)
                .moderatedAt(LocalDateTime.now())
                .processTimeMs(processTime)
                .build();
    }

    private ModerationResponse handleError(ContentModerationLog log, Exception e, long startTime) {
        long processTime = System.currentTimeMillis() - startTime;
        
        log.setModerationStatus(ContentModerationLog.ModerationStatus.ERROR);
        log.setViolationDetails("审核异常: " + e.getMessage());
        log.setProcessTimeMs(processTime);
        log.setRetryCount(log.getRetryCount() + 1);
        
        moderationLogRepository.save(log);
        
        return ModerationResponse.builder()
                .logId(log.getId())
                .status(ContentModerationLog.ModerationStatus.ERROR)
                .statusDescription("审核异常")
                .approved(false)
                .shouldBlock(true)
                .violationDetails(e.getMessage())
                .suggestedAction(ModerationResponse.SuggestedAction.RETRY)
                .moderatedAt(LocalDateTime.now())
                .processTimeMs(processTime)
                .build();
    }

    private List<ModerationResponse.HitWordInfo> convertHitsToResponse(
            List<SensitiveWordService.SensitiveWordHitResult> hits) {
        List<ModerationResponse.HitWordInfo> result = new ArrayList<>();
        
        for (SensitiveWordService.SensitiveWordHitResult hit : hits) {
            List<ModerationResponse.HitWordInfo.Position> positions = new ArrayList<>();
            for (SensitiveWordService.Position pos : hit.positions) {
                positions.add(ModerationResponse.HitWordInfo.Position.builder()
                        .start(pos.start)
                        .end(pos.end)
                        .build());
            }
            
            result.add(ModerationResponse.HitWordInfo.builder()
                    .wordId(hit.wordId)
                    .word(hit.word)
                    .category(hit.category.name())
                    .level(hit.level.name())
                    .positions(positions)
                    .build());
        }
        
        return result;
    }

    private ContentModerationLog.ViolationType mapViolationType(String apiViolationType) {
        if (apiViolationType == null) return ContentModerationLog.ViolationType.OTHER;
        
        return switch (apiViolationType.toLowerCase()) {
            case "politics" -> ContentModerationLog.ViolationType.POLITICS;
            case "porn", "sexy" -> ContentModerationLog.ViolationType.PORNOGRAPHY;
            case "violence" -> ContentModerationLog.ViolationType.VIOLENCE;
            case "terrorism" -> ContentModerationLog.ViolationType.TERRORISM;
            case "gambling" -> ContentModerationLog.ViolationType.GAMBLING;
            case "fraud" -> ContentModerationLog.ViolationType.FRAUD;
            case "abuse" -> ContentModerationLog.ViolationType.ABUSE;
            case "ad" -> ContentModerationLog.ViolationType.ADVERTISEMENT;
            default -> ContentModerationLog.ViolationType.OTHER;
        };
    }

    private ModerationResponse convertToResponse(ContentModerationLog log) {
        return ModerationResponse.builder()
                .logId(log.getId())
                .status(log.getModerationStatus())
                .statusDescription(log.getStatusDescription())
                .approved(log.isApproved())
                .shouldBlock(log.shouldBlock())
                .violationType(log.getViolationType())
                .violationDetails(log.getViolationDetails())
                .moderationType(log.getModerationType())
                .provider(log.getProvider())
                .needManualReview(log.getManualReviewRequired())
                .moderatedAt(log.getCreatedAt())
                .processTimeMs(log.getProcessTimeMs())
                .build();
    }
}