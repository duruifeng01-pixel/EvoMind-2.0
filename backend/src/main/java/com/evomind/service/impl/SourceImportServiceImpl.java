package com.evomind.service.impl;

import com.evomind.dto.request.ConfirmImportRequest;
import com.evomind.dto.request.LinkImportRequest;
import com.evomind.dto.request.OcrImportRequest;
import com.evomind.dto.response.SourceImportJobResponse;
import com.evomind.entity.Source;
import com.evomind.entity.SourceImportJob;
import com.evomind.exception.BusinessException;
import com.evomind.exception.ResourceNotFoundException;
import com.evomind.repository.SourceImportJobRepository;
import com.evomind.repository.SourceRepository;
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

/**
 * 信息源导入服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SourceImportServiceImpl implements SourceImportService {

    private final SourceImportJobRepository jobRepository;
    private final SourceRepository sourceRepository;
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
        job.setImageUrl("data:image/" + ocrRequest.getImageFormat() + ";base64," + ocrRequest.getImageBase64().substring(0, Math.min(100, ocrRequest.getImageBase64().length())) + "...");
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        // 保存任务
        SourceImportJob savedJob = jobRepository.save(job);

        // TODO: 异步调用OCR服务识别图片中的博主信息
        // 这里先返回任务，实际识别逻辑将在后续集成百度OCR SDK后实现
        log.info("用户 {} 提交OCR导入任务 {}，待实现OCR识别逻辑", userId, savedJob.getId());

        // 模拟处理完成（实际应该异步处理）
        simulateOcrProcessing(savedJob);

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

        // TODO: 异步调用链接抓取服务
        // 这里先返回任务，实际抓取逻辑将在后续实现
        log.info("用户 {} 提交链接抓取任务 {}，待实现抓取逻辑", userId, savedJob.getId());

        // 模拟处理完成（实际应该异步处理）
        simulateLinkProcessing(savedJob);

        return SourceImportJobResponse.fromEntity(savedJob);
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
    @Transactional
    public List<ImportResult> confirmImport(Long userId, ConfirmImportRequest confirmRequest) {
        SourceImportJob job = jobRepository.findByIdAndUserId(confirmRequest.getJobId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("导入任务不存在"));

        if (job.getStatus() != SourceImportJob.JobStatus.COMPLETED) {
            throw new BusinessException("任务尚未完成，无法确认导入");
        }

        List<ImportResult> results = new ArrayList<>();

        for (ConfirmImportRequest.SelectedAuthor author : confirmRequest.getSelectedAuthors()) {
            // 检查是否已存在相同名称的信息源
            Optional<Source> existingSource = sourceRepository.findByUserIdAndName(userId, author.getName());

            ImportResult result = new ImportResult();
            result.setName(author.getName());
            result.setPlatform(author.getPlatform());

            if (existingSource.isPresent()) {
                // 已存在，不重复添加
                result.setSourceId(existingSource.get().getId());
                result.setExisted(true);
                log.info("用户 {} 的信息源 '{}' 已存在，跳过添加", userId, author.getName());
            } else {
                // 创建新信息源
                Source source = new Source();
                source.setUserId(userId);
                source.setName(author.getName());
                source.setPlatform(author.getPlatform());
                source.setHomeUrl(author.getHomeUrl());
                source.setCategory(author.getCategory());
                source.setEnabled(true);
                source.setArticleCount(0);

                Source saved = sourceRepository.save(source);
                result.setSourceId(saved.getId());
                result.setExisted(false);
                log.info("用户 {} 成功添加信息源 '{}'，ID: {}", userId, author.getName(), saved.getId());
            }

            results.add(result);
        }

        // 更新任务状态为已确认
        job.setSelectedAuthorsJson(toJson(confirmRequest.getSelectedAuthors()));
        jobRepository.save(job);

        return results;
    }

    @Override
    @Transactional
    public void cancelJob(Long userId, Long jobId) {
        SourceImportJob job = jobRepository.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("导入任务不存在"));

        // 只能取消待处理或处理中的任务
        if (job.getStatus() == SourceImportJob.JobStatus.PENDING ||
                job.getStatus() == SourceImportJob.JobStatus.PROCESSING) {
            job.setStatus(SourceImportJob.JobStatus.FAILED);
            job.setErrorMessage("用户取消任务");
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            log.info("用户 {} 取消导入任务 {}", userId, jobId);
        } else {
            throw new BusinessException("任务状态不允许取消");
        }
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
        jobRepository.save(job);

        // 根据类型重新触发处理
        if (job.getImportType() == SourceImportJob.ImportType.OCR_SCREENSHOT) {
            simulateOcrProcessing(job);
        } else {
            simulateLinkProcessing(job);
        }

        return SourceImportJobResponse.fromEntity(job);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDailyLimitExceeded(Long userId, int maxLimit) {
        Long todayCount = jobRepository.countTodayImportsByUserId(userId);
        return todayCount >= maxLimit;
    }

    /**
     * 检测链接所属平台
     */
    private String detectPlatform(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains("xiaohongshu.com") || lowerUrl.contains("xhslink.com")) {
            return "xiaohongshu";
        } else if (lowerUrl.contains("mp.weixin.qq.com") || lowerUrl.contains("weixin.qq.com")) {
            return "weixin";
        } else if (lowerUrl.contains("zhihu.com")) {
            return "zhihu";
        } else if (lowerUrl.contains("douyin.com") || lowerUrl.contains("iesdouyin.com")) {
            return "douyin";
        }
        return "unknown";
    }

    /**
     * 模拟OCR处理（待替换为实际OCR SDK调用）
     */
    @SneakyThrows
    private void simulateOcrProcessing(SourceImportJob job) {
        job.startProcessing();
        jobRepository.save(job);

        // 模拟异步处理延迟
        Thread.sleep(500);

        // TODO: 集成百度OCR SDK后替换为实际识别逻辑
        // 模拟检测到博主
        List<SourceImportJobResponse.DetectedAuthor> detectedAuthors = new ArrayList<>();
        SourceImportJobResponse.DetectedAuthor author = new SourceImportJobResponse.DetectedAuthor();
        author.setName("示例博主");
        author.setAvatarUrl("https://example.com/avatar.jpg");
        author.setConfidence(0.95);
        author.setPlatform("xiaohongshu");
        author.setHomeUrl("https://www.xiaohongshu.com/user/example");
        detectedAuthors.add(author);

        job.setDetectedAuthorsJson(toJson(detectedAuthors));
        job.setPlatform("xiaohongshu");
        job.complete();
        jobRepository.save(job);

        log.info("OCR任务 {} 模拟处理完成", job.getId());
    }

    /**
     * 模拟链接抓取（待替换为实际抓取逻辑）
     */
    @SneakyThrows
    private void simulateLinkProcessing(SourceImportJob job) {
        job.startProcessing();
        jobRepository.save(job);

        // 模拟异步处理延迟
        Thread.sleep(500);

        // TODO: 实现实际链接抓取逻辑
        // 模拟抓取到作者信息
        List<SourceImportJobResponse.DetectedAuthor> detectedAuthors = new ArrayList<>();
        SourceImportJobResponse.DetectedAuthor author = new SourceImportJobResponse.DetectedAuthor();
        author.setName("链接作者");
        author.setAvatarUrl("https://example.com/avatar2.jpg");
        author.setConfidence(1.0);
        author.setPlatform(job.getPlatform());
        author.setHomeUrl(job.getSourceUrl());
        detectedAuthors.add(author);

        job.setDetectedAuthorsJson(toJson(detectedAuthors));
        job.complete();
        jobRepository.save(job);

        log.info("链接抓取任务 {} 模拟处理完成", job.getId());
    }

    @SneakyThrows
    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }
}
