package com.evomind.service;

import com.evomind.dto.request.ModerationRequest;
import com.evomind.dto.response.ModerationResponse;
import com.evomind.entity.ContentModerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 内容审核服务接口
 * 提供敏感词过滤、AI内容审核、日志记录等功能
 */
public interface ContentModerationService {

    /**
     * 同步审核内容
     * 
     * @param userId 用户ID
     * @param request 审核请求
     * @return 审核结果
     */
    ModerationResponse moderateContent(Long userId, ModerationRequest request);

    /**
     * 异步审核内容（用于非阻塞场景）
     * 
     * @param userId 用户ID
     * @param request 审核请求
     * @return 异步审核结果
     */
    CompletableFuture<ModerationResponse> moderateContentAsync(Long userId, ModerationRequest request);

    /**
     * 快速检测（仅敏感词过滤，不调用第三方API）
     * 
     * @param content 待检测内容
     * @return 是否包含敏感词
     */
    boolean quickCheck(String content);

    /**
     * 快速检测并返回命中信息
     * 
     * @param content 待检测内容
     * @return 命中的敏感词列表
     */
    List<ModerationResponse.HitWordInfo> quickCheckWithDetails(String content);

    /**
     * AI生成内容专用审核（自动标记为AI生成）
     * 
     * @param userId 用户ID
     * @param content 内容
     * @param contentType 内容类型
     * @param aiModel AI模型名称
     * @return 审核结果
     */
    ModerationResponse moderateAiGeneratedContent(Long userId, String content, 
                                                   ContentModerationLog.ContentType contentType,
                                                   String aiModel);

    /**
     * 用户发布内容审核
     * 
     * @param userId 用户ID
     * @param content 内容
     * @param contentType 内容类型
     * @return 审核结果
     */
    ModerationResponse moderateUserContent(Long userId, String content,
                                           ContentModerationLog.ContentType contentType);

    /**
     * 获取审核记录详情
     * 
     * @param logId 记录ID
     * @return 审核日志
     */
    ContentModerationLog getModerationLog(Long logId);

    /**
     * 根据内容ID获取最新审核结果
     * 
     * @param contentId 内容ID
     * @param contentType 内容类型
     * @return 审核结果
     */
    ModerationResponse getModerationResult(String contentId, ContentModerationLog.ContentType contentType);

    /**
     * 查询用户审核历史
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 审核日志分页
     */
    Page<ContentModerationLog> getUserModerationHistory(Long userId, Pageable pageable);

    /**
     * 人工复核
     * 
     * @param logId 审核记录ID
     * @param reviewerId 复核人ID
     * @param result 复核结果
     * @param remark 复核备注
     * @return 更新后的审核结果
     */
    ModerationResponse manualReview(Long logId, Long reviewerId, 
                                    ContentModerationLog.ModerationStatus result,
                                    String remark);

    /**
     * 批量审核（用于历史数据处理）
     * 
     * @param userId 用户ID
     * @param requests 审核请求列表
     * @return 审核结果列表
     */
    List<ModerationResponse> batchModerate(Long userId, List<ModerationRequest> requests);

    /**
     * 获取审核统计信息
     * 
     * @param userId 用户ID（null表示全局统计）
     * @return 统计数据
     */
    ModerationStatistics getStatistics(Long userId);

    /**
     * 重新审核内容
     * 
     * @param logId 原审核记录ID
     * @param reviewerId 操作人ID
     * @return 新的审核结果
     */
    ModerationResponse reModerate(Long logId, Long reviewerId);

    /**
     * 审核统计信息
     */
    class ModerationStatistics {
        public long totalCount;
        public long approvedCount;
        public long rejectedCount;
        public long pendingCount;
        public long needReviewCount;
        public double approvalRate;
        public double rejectionRate;
    }
}