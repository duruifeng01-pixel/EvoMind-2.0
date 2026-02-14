package com.evomind.service.impl;

import com.evomind.dto.request.ConfirmImportRequest;
import com.evomind.dto.request.LinkImportRequest;
import com.evomind.dto.request.OcrImportRequest;
import com.evomind.dto.response.OcrResultResponse;
import com.evomind.dto.response.SourceImportJobResponse;
import com.evomind.entity.OcrImportLog;
import com.evomind.entity.Source;
import com.evomind.entity.SourceImportJob;
import com.evomind.exception.BusinessException;
import com.evomind.exception.ResourceNotFoundException;
import com.evomind.repository.OcrImportLogRepository;
import com.evomind.repository.SourceImportJobRepository;
import com.evomind.repository.SourceRepository;
import com.evomind.service.OcrService;
import com.evomind.service.SourceImportService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 信息源导入服务实现
 * 支持OCR截图识别和链接抓取两种方式导入信息源
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourceImportServiceImpl implements SourceImportService {

    private final SourceImportJobRepository jobRepository;
    private final OcrImportLogRepository ocrImportLogRepository;
    private final SourceRepository sourceRepository;
    private final OcrService ocrService;
    private final ObjectMapper objectMapper;

    // 每日导入限制（免费用户）
    private static final int DAILY_IMPORT_LIMIT = 20;

    @Override
    @Transactional
    public SourceImportJobResponse submitOcrTask(Long userId, OcrImportRequest ocrRequest) {
        // 检查每日限制
        if (isDailyLimitExceeded(userId, DAILY_IMPORT_LIMIT)) {
            throw new BusinessException("今日导入次数已达上限，请明天再试");
        }

        // 创建任务
        SourceImportJob job = new SourceImportJob();
        job.setUserId(userId);
        job.setImportType(SourceImportJob.ImportType.OCR_SCREENSHOT);
        job.setStatus(SourceImportJob.JobStatus.PENDING);
        job.setPlatform(ocrRequest.getPlatform());
        job.setImageUrl("data:image/" + ocrRequest.getImageFormat() + ";base64," + 
            ocrRequest.getImageBase64().substring(0, Math.min(50, ocrRequest.getImageBase64().length())) + "...");
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        // 保存任务
        SourceImportJob savedJob = jobRepository.save(job);

        // 立即执行OCR识别（同步处理，实际可改为异步）
        try {
            executeOcrRecognition(savedJob, ocrRequest);
        } catch (Exception e) {
            log.error("OCR识别失败", e);
            savedJob.fail(e.getMessage());
            jobRepository.save(savedJob);
        }

        return SourceImportJobResponse.fromEntity(savedJob);
    }

    @Override
    @Transactional
    public SourceImportJobResponse submitLinkTask(Long userId, LinkImportRequest linkRequest) {
        // 检查每日限制
        if (isDailyLimitExceeded(userId, DAILY_IMPORT_LIMIT)) {
            throw new BusinessException("今日导入次数已达上限，请明天再试");
        }

        // 解析平台类型
        String platform = detectPlatform(linkRequest.getUrl());
        if (linkRequest.getExpectedPlatform() != null && !platform.equals(linkRequest.getExpectedPlatform())) {
            throw new BusinessException("链接平台与期望平台不匹配");
        }

        // 创建任务
        SourceImportJob job = new SourceImportJob();
        job.setUserId(userId);
        job.setImportType(SourceImportJob.ImportType.LINK_SCRAPE);
        job.setStatus(SourceImportJob.JobStatus.PENDING);
        job.setSourceUrl(linkRequest.getUrl());
        job.setPlatform(platform);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        SourceImportJob savedJob = jobRepository.save(job);

        // TODO: 异步调用链接抓取服务（feat_005实现）
        log.info("用户 {} 提交链接抓取任务 {}，待实现抓取逻辑", userId, savedJob.getId());

        // 模拟处理完成（实际应该异步处理）
        simulateLinkProcessing(savedJob);

        return SourceImportJobResponse.fromEntity(savedJob);
    }

    /**
     * 执行OCR识别
     */
    private void executeOcrRecognition(SourceImportJob job, OcrImportRequest request) throws Exception {
        job.startProcessing();
        jobRepository.save(job);

        // 调用OCR服务识别
        OcrResultResponse ocrResult = ocrService.recognizeBloggers(job.getUserId(), request);

        // 转换结果格式
        List<SourceImportJobResponse.DetectedAuthor> authors = ocrResult.getBloggers().stream()
            .map(blogger -> {
                SourceImportJobResponse.DetectedAuthor author = new SourceImportJobResponse.DetectedAuthor();
                author.setName(blogger.getName());
                author.setAvatarUrl(blogger.getAvatarUrl());
                author.setConfidence(blogger.getConfidence());
                author.setHomeUrl(blogger.getHomeUrl());
                author.setPlatform(blogger.getPlatform());
                return author;
            })
            .collect(Collectors.toList());

        // 保存识别结果
        job.setDetectedAuthorsJson(objectMapper.writeValueAsString(authors));
        job.setPlatform(request.getPlatform());
        job.complete();
        jobRepository.save(job);

        log.info("OCR识别完成: jobId={}, 识别到{}个博主", job.getId(), authors.size());
    }

    @Override
    @Transactional(readOnly = true)
    public SourceImportJobResponse getJobStatus(Long userId, Long jobId) {
        SourceImportJob job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("导入任务不存在"));

        SourceImportJobResponse response = SourceImportJobResponse.fromEntity(job);

        // 解析检测到的作者JSON
        if (job.getDetectedAuthorsJson() != null && !job.getDetectedAuthorsJson().isEmpty()) {
            try {
                List<SourceImportJobResponse.DetectedAuthor> authors = objectMapper.readValue(
                        job.getDetectedAuthorsJson(),
                        new TypeReference<List<SourceImportJobResponse.DetectedAuthor>>() {}
                );
                response.setDetectedAuthors(authors);
            } catch (Exception e) {
                log.warn("解析检测到的作者JSON失败: {}", e.getMessage());
            }
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SourceImportJobResponse> getUserJobs(Long userId, Pageable pageable) {
        return jobRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(SourceImportJobResponse::fromEntity);
    }

    @Override
    @SneakyThrows
    @Transactional
    public List<ImportResult> confirmImport(Long userId, ConfirmImportRequest confirmRequest) {
        // 从OCR任务中获取结果
        OcrImportLog ocrLog = ocrImportLogRepository.findByTaskId(confirmRequest.getTaskId())
            .orElseThrow(() -> new ResourceNotFoundException("OCR任务不存在"));

        if (ocrLog.getStatus() != OcrImportLog.OcrStatus.SUCCESS) {
            throw new BusinessException("OCR任务未完成或失败");
        }

        // 解析OCR结果
        OcrResultResponse ocrResult = objectMapper.readValue(ocrLog.getRawResult(), OcrResultResponse.class);

        List<ImportResult> results = new ArrayList<>();
        int importedCount = 0;

        // 导入选中的博主
        for (OcrResultResponse.DetectedBlogger blogger : ocrResult.getBloggers()) {
            if (!confirmRequest.getSelectedCandidateIds().contains(blogger.getCandidateId())) {
                continue;
            }

            // 检查是否已存在
            Optional<Source> existingSource = sourceRepository.findByUserIdAndHomeUrl(
                userId, blogger.getHomeUrl());

            ImportResult result = new ImportResult();
            result.setName(blogger.getName());
            result.setPlatform(blogger.getPlatform());

            if (existingSource.isPresent()) {
                result.setSourceId(existingSource.get().getId());
                result.setExisted(true);
            } else {
                // 创建新信息源
                Source source = new Source();
                source.setUserId(userId);
                source.setName(blogger.getName());
                source.setPlatform(blogger.getPlatform());
                source.setHomeUrl(blogger.getHomeUrl());
                source.setCategory("imported");
                source.setSyncStatus("pending");
                source.setEnabled(true);

                Source saved = sourceRepository.save(source);
                result.setSourceId(saved.getId());
                result.setExisted(false);
                importedCount++;
            }

            results.add(result);
        }

        // 更新OCR日志状态
        ocrLog.setStatus(OcrImportLog.OcrStatus.CONFIRMED);
        ocrLog.setImportedCount(importedCount);
        ocrLog.setConfirmedAt(LocalDateTime.now());
        ocrImportLogRepository.save(ocrLog);

        log.info("用户 {} 确认导入: taskId={}, 选中{}个, 成功导入{}个",
            userId, confirmRequest.getTaskId(), confirmRequest.getSelectedCandidateIds().size(), importedCount);

        return results;
    }

    @Override
    @Transactional
    public void cancelJob(Long userId, Long jobId) {
        SourceImportJob job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("导入任务不存在"));

        if (job.getStatus() == SourceImportJob.JobStatus.COMPLETED ||
            job.getStatus() == SourceImportJob.JobStatus.FAILED) {
            throw new BusinessException("已完成的任务无法取消");
        }

        job.setStatus(SourceImportJob.JobStatus.FAILED);
        job.setErrorMessage("用户取消");
        job.setCompletedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    @Override
    @Transactional
    public SourceImportJobResponse retryJob(Long userId, Long jobId) {
        SourceImportJob job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("导入任务不存在"));

        if (job.getStatus() != SourceImportJob.JobStatus.FAILED) {
            throw new BusinessException("只有失败的任务可以重试");
        }

        job.retry();
        job.setErrorMessage(null);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        // 实际应该触发异步重新处理
        log.info("任务重试: jobId={}", jobId);

        return SourceImportJobResponse.fromEntity(job);
    }

    @Override
    public boolean isDailyLimitExceeded(Long userId, int maxLimit) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayCount = jobRepository.countByUserIdAndCreatedAtAfter(userId, startOfDay);
        return todayCount >= maxLimit;
    }

    /**
     * 检测链接平台类型
     */
    private String detectPlatform(String url) {
        if (url.contains("xiaohongshu.com") || url.contains("xhslink.com")) {
            return "xiaohongshu";
        } else if (url.contains("mp.weixin.qq.com") || url.contains("weixin.qq.com")) {
            return "weixin";
        } else if (url.contains("zhihu.com")) {
            return "zhihu";
        } else if (url.contains("douyin.com") || url.contains("iesdouyin.com")) {
            return "douyin";
        }
        return "other";
    }

    /**
     * 模拟链接处理（待feat_005实现真实逻辑）
     */
    private void simulateLinkProcessing(SourceImportJob job) {
        job.startProcessing();
        job.setDetectedAuthorsJson("[]");
        job.complete();
        jobRepository.save(job);
    }
}
