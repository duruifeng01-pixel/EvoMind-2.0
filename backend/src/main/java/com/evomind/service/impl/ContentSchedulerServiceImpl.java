package com.evomind.service.impl;

import com.evomind.entity.ContentCrawlJob;
import com.evomind.entity.Source;
import com.evomind.repository.ContentCrawlJobRepository;
import com.evomind.repository.SourceRepository;
import com.evomind.service.ContentCrawlService;
import com.evomind.service.ContentSchedulerService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内容采集调度服务实现
 * 负责定时调度内容采集任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentSchedulerServiceImpl implements ContentSchedulerService {

    private final ContentCrawlJobRepository crawlJobRepository;
    private final SourceRepository sourceRepository;
    private final ContentCrawlService contentCrawlService;
    private final ThreadPoolTaskScheduler taskScheduler;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong totalJobsExecuted = new AtomicLong(0);
    private final AtomicLong totalJobsFailed = new AtomicLong(0);
    private volatile ScheduledFuture<?> scheduledTask;

    private ScheduleConfig currentConfig;
    private LocalDateTime lastExecutionTime;
    private LocalDateTime nextScheduledTime;

    @PostConstruct
    public void init() {
        // 初始化默认配置
        currentConfig = new ScheduleConfig();
        currentConfig.setIntervalMinutes(30);
        currentConfig.setBatchSize(10);
        currentConfig.setRetryAttempts(3);
        currentConfig.setEnabled(true);

        if (currentConfig.isEnabled()) {
            startScheduler();
        }
    }

    @PreDestroy
    public void destroy() {
        stopScheduler();
    }

    @Override
    public void startScheduler() {
        if (isRunning.compareAndSet(false, true)) {
            log.info("Starting content crawl scheduler with interval: {} minutes", 
                    currentConfig.getIntervalMinutes());

            // 使用固定延迟调度
            scheduledTask = taskScheduler.scheduleWithFixedDelay(
                    this::executeScheduledCrawl,
                    currentConfig.getIntervalMinutes() * 60 * 1000
            );

            nextScheduledTime = LocalDateTime.now().plusMinutes(currentConfig.getIntervalMinutes());
            log.info("Content crawl scheduler started");
        } else {
            log.warn("Scheduler is already running");
        }
    }

    @Override
    public void stopScheduler() {
        if (isRunning.compareAndSet(true, false)) {
            if (scheduledTask != null && !scheduledTask.isCancelled()) {
                scheduledTask.cancel(false);
                scheduledTask = null;
            }
            log.info("Content crawl scheduler stopped");
        }
    }

    /**
     * 执行定时采集任务
     */
    private void executeScheduledCrawl() {
        if (!isRunning.get()) {
            return;
        }

        lastExecutionTime = LocalDateTime.now();
        log.info("Executing scheduled content crawl at {}", 
                lastExecutionTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        try {
            // 获取所有启用的信息源
            List<Source> allSources = sourceRepository.findAll().stream()
                    .filter(s -> Boolean.TRUE.equals(s.getEnabled()))
                    .toList();

            if (allSources.isEmpty()) {
                log.info("No enabled sources found for crawling");
                return;
            }

            // 分批处理
            int batchSize = currentConfig.getBatchSize();
            int totalSources = allSources.size();
            int processedCount = 0;

            for (int i = 0; i < totalSources; i += batchSize) {
                List<Source> batch = allSources.subList(i, Math.min(i + batchSize, totalSources));
                
                for (Source source : batch) {
                    try {
                        // 检查是否需要更新（根据上次同步时间）
                        if (shouldCrawlSource(source)) {
                            ContentCrawlJob job = contentCrawlService.createCrawlJob(
                                    source.getId(), source.getUserId());
                            contentCrawlService.executeCrawlJob(job.getId());
                            totalJobsExecuted.incrementAndGet();
                            processedCount++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to crawl source: {}", source.getId(), e);
                        totalJobsFailed.incrementAndGet();
                    }
                }

                // 批次间延迟
                if (i + batchSize < totalSources) {
                    Thread.sleep(5000);
                }
            }

            log.info("Scheduled crawl completed. Processed {}/{} sources", processedCount, totalSources);

        } catch (Exception e) {
            log.error("Scheduled crawl failed", e);
            totalJobsFailed.incrementAndGet();
        }

        // 重试失败的任务
        retryFailedJobs();

        nextScheduledTime = LocalDateTime.now().plusMinutes(currentConfig.getIntervalMinutes());
    }

    /**
     * 判断信息源是否需要采集
     */
    private boolean shouldCrawlSource(Source source) {
        // 如果没有同步过，需要采集
        if (source.getLastSyncAt() == null) {
            return true;
        }

        // 如果上次同步超过1小时，需要重新采集
        return source.getLastSyncAt().isBefore(LocalDateTime.now().minusHours(1));
    }

    /**
     * 重试失败的任务
     */
    private void retryFailedJobs() {
        try {
            List<ContentCrawlJob> failedJobs = crawlJobRepository.findFailedJobsForRetry(
                    ContentCrawlJob.JobStatus.FAILED, currentConfig.getRetryAttempts());

            for (ContentCrawlJob job : failedJobs) {
                try {
                    log.info("Retrying failed job: {}", job.getId());
                    contentCrawlService.executeCrawlJob(job.getId());
                } catch (Exception e) {
                    log.error("Retry failed for job: {}", job.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error during job retry", e);
        }
    }

    @Override
    @Transactional
    public List<ContentCrawlJob> triggerImmediateCrawl() {
        log.info("Triggering immediate crawl for all enabled sources");
        
        List<Source> enabledSources = sourceRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getEnabled()))
                .toList();

        List<Long> sourceIds = enabledSources.stream()
                .map(Source::getId)
                .toList();

        return contentCrawlService.executeBatchCrawl(sourceIds);
    }

    @Override
    @Transactional
    public List<ContentCrawlJob> triggerCrawlForUser(Long userId) {
        log.info("Triggering crawl for user: {}", userId);
        
        List<Source> userSources = sourceRepository.findByUserIdAndEnabledTrueOrderByIsPinnedDescCreatedAtDesc(userId);
        
        List<ContentCrawlJob> jobs = new ArrayList<>();
        for (Source source : userSources) {
            try {
                ContentCrawlJob job = contentCrawlService.createCrawlJob(source.getId(), userId);
                job = contentCrawlService.executeCrawlJob(job.getId());
                jobs.add(job);
            } catch (Exception e) {
                log.error("Failed to crawl source {} for user {}", source.getId(), userId, e);
            }
        }

        return jobs;
    }

    @Override
    @Transactional
    public ContentCrawlJob triggerCrawlForSource(Long sourceId) {
        log.info("Triggering crawl for source: {}", sourceId);
        
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new RuntimeException("Source not found: " + sourceId));

        ContentCrawlJob job = contentCrawlService.createCrawlJob(sourceId, source.getUserId());
        return contentCrawlService.executeCrawlJob(job.getId());
    }

    @Override
    public SchedulerStatus getSchedulerStatus() {
        SchedulerStatus status = new SchedulerStatus();
        status.setRunning(isRunning.get());
        status.setTotalJobsExecuted(totalJobsExecuted.get());
        status.setTotalJobsFailed(totalJobsFailed.get());
        status.setLastExecutionTime(lastExecutionTime != null ? 
                lastExecutionTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        status.setNextScheduledTime(nextScheduledTime != null ? 
                nextScheduledTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        return status;
    }

    @Override
    public void updateScheduleConfig(ScheduleConfig config) {
        this.currentConfig = config;
        
        // 如果调度器正在运行，需要重启以应用新配置
        if (isRunning.get()) {
            stopScheduler();
            if (config.isEnabled()) {
                startScheduler();
            }
        } else if (config.isEnabled()) {
            startScheduler();
        }
        
        log.info("Schedule config updated: interval={}, batchSize={}, enabled={}",
                config.getIntervalMinutes(), config.getBatchSize(), config.isEnabled());
    }

    @Override
    public List<ContentCrawlJob> getPendingJobs() {
        return crawlJobRepository.findAll().stream()
                .filter(j -> j.getStatus() == ContentCrawlJob.JobStatus.PENDING ||
                            j.getStatus() == ContentCrawlJob.JobStatus.RUNNING)
                .toList();
    }

    @Override
    public List<ContentCrawlJob> getRecentJobs(int limit) {
        return crawlJobRepository.findAll(PageRequest.of(0, limit, 
                org.springframework.data.domain.Sort.by("createdAt").descending()))
                .getContent();
    }
}
