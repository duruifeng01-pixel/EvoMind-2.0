package com.evomind.service.impl;

import com.evomind.dto.response.LinkScrapeResponse;
import com.evomind.entity.ContentCrawlJob;
import com.evomind.entity.CrawledContent;
import com.evomind.entity.Source;
import com.evomind.repository.ContentCrawlJobRepository;
import com.evomind.repository.CrawledContentRepository;
import com.evomind.repository.SourceRepository;
import com.evomind.service.ContentCrawlService;
import com.evomind.service.LinkScrapeService;
import com.evomind.service.ContentDeduplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 内容采集服务实现
 * 负责从信息源抓取内容
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentCrawlServiceImpl implements ContentCrawlService {

    private final ContentCrawlJobRepository crawlJobRepository;
    private final CrawledContentRepository crawledContentRepository;
    private final SourceRepository sourceRepository;
    private final LinkScrapeService linkScrapeService;
    private final ContentDeduplicationService deduplicationService;

    @Override
    @Transactional
    public ContentCrawlJob createCrawlJob(Long sourceId, Long userId) {
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new RuntimeException("Source not found: " + sourceId));

        // 检查是否已有正在执行的任务
        List<ContentCrawlJob> runningJobs = crawlJobRepository.findBySourceIdAndStatus(
                sourceId, ContentCrawlJob.JobStatus.RUNNING);
        if (!runningJobs.isEmpty()) {
            log.warn("Source {} already has running crawl job", sourceId);
            return runningJobs.get(0);
        }

        ContentCrawlJob job = new ContentCrawlJob();
        job.setSourceId(sourceId);
        job.setUserId(userId);
        job.setPlatform(source.getPlatform());
        job.setSourceUrl(source.getHomeUrl());
        job.setJobType(ContentCrawlJob.JobType.SCHEDULED);
        job.setStatus(ContentCrawlJob.JobStatus.PENDING);
        job.setArticlesFound(0);
        job.setArticlesNew(0);
        job.setArticlesDuplicated(0);
        job.setRetryCount(0);

        return crawlJobRepository.save(job);
    }

    @Override
    @Transactional
    public ContentCrawlJob executeCrawlJob(Long jobId) {
        ContentCrawlJob job = crawlJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Crawl job not found: " + jobId));

        if (job.getStatus() == ContentCrawlJob.JobStatus.RUNNING) {
            log.warn("Job {} is already running", jobId);
            return job;
        }

        // 更新状态为执行中
        job.setStatus(ContentCrawlJob.JobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        job = crawlJobRepository.save(job);

        try {
            Source source = sourceRepository.findById(job.getSourceId())
                    .orElseThrow(() -> new RuntimeException("Source not found: " + job.getSourceId()));

            // 抓取内容
            List<CrawledContent> crawledContents = fetchLatestContent(source, 20);

            int newCount = 0;
            int duplicateCount = 0;

            for (CrawledContent content : crawledContents) {
                content.setSourceId(source.getId());
                content.setUserId(job.getUserId());
                content.setPlatform(source.getPlatform());
                content.setIsSystemDiscovered(false);

                // 去重检查
                ContentDeduplicationService.DeduplicationResult dedupResult = 
                        deduplicationService.deduplicate(content);

                if (dedupResult.isDuplicate()) {
                    content.setIsDuplicate(true);
                    content.setDuplicateOfId(dedupResult.getDuplicateOfId());
                    content.setStatus(CrawledContent.ContentStatus.REJECTED);
                    duplicateCount++;
                } else {
                    content.setStatus(CrawledContent.ContentStatus.DEDUPLICATED);
                    newCount++;
                }

                crawledContentRepository.save(content);
            }

            // 更新任务状态
            job.setStatus(ContentCrawlJob.JobStatus.COMPLETED);
            job.setArticlesFound(crawledContents.size());
            job.setArticlesNew(newCount);
            job.setArticlesDuplicated(duplicateCount);
            job.setCompletedAt(LocalDateTime.now());

            // 更新源的同步状态
            sourceRepository.updateSyncStatus(source.getId(), "COMPLETED");
            sourceRepository.updateArticleCount(source.getId(), newCount);

            log.info("Crawl job completed: jobId={}, sourceId={}, found={}, new={}, duplicated={}",
                    jobId, source.getId(), crawledContents.size(), newCount, duplicateCount);

        } catch (Exception e) {
            log.error("Crawl job failed: jobId={}", jobId, e);
            job.setStatus(ContentCrawlJob.JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            job.setRetryCount(job.getRetryCount() + 1);

            sourceRepository.updateSyncStatus(job.getSourceId(), "FAILED");
        }

        return crawlJobRepository.save(job);
    }

    @Override
    @Transactional
    public List<ContentCrawlJob> executeBatchCrawl(List<Long> sourceIds) {
        List<ContentCrawlJob> jobs = new ArrayList<>();

        for (Long sourceId : sourceIds) {
            try {
                Source source = sourceRepository.findById(sourceId).orElse(null);
                if (source == null || !Boolean.TRUE.equals(source.getEnabled())) {
                    log.warn("Source {} not found or disabled, skipping", sourceId);
                    continue;
                }

                ContentCrawlJob job = createCrawlJob(sourceId, source.getUserId());
                job = executeCrawlJob(job.getId());
                jobs.add(job);

                // 添加延迟避免请求过快
                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("Failed to crawl source: {}", sourceId, e);
            }
        }

        return jobs;
    }

    @Override
    public CrawledContent crawlContentFromUrl(String url, String platform) {
        try {
            // 使用LinkScrapeService抓取内容
            LinkScrapeResponse response = linkScrapeService.scrapeUrl(url, null);

            if (!"SUCCESS".equals(response.getStatus())) {
                log.error("Failed to crawl URL: {}, error: {}", url, response.getErrorMessage());
                return null;
            }

            CrawledContent content = new CrawledContent();
            content.setOriginalUrl(url);
            content.setPlatform(platform);
            content.setTitle(response.getTitle());
            content.setContent(response.getContent());
            content.setAuthor(response.getAuthor());
            content.setPublishedAt(response.getPublishTime());
            content.setCrawledAt(LocalDateTime.now());
            content.setStatus(CrawledContent.ContentStatus.RAW);
            content.setIsDuplicate(false);
            content.setIsSystemDiscovered(true);

            return content;

        } catch (Exception e) {
            log.error("Error crawling URL: {}", url, e);
            return null;
        }
    }

    @Override
    public String parseContent(String rawHtml, String platform) {
        // 内容解析逻辑已经在LinkScrapeService中实现
        // 这里可以添加额外的解析逻辑
        return rawHtml;
    }

    @Override
    public List<CrawledContent> fetchLatestContent(Source source, int limit) {
        List<CrawledContent> contents = new ArrayList<>();

        try {
            // 根据平台类型选择抓取策略
            String platform = source.getPlatform();
            String homeUrl = source.getHomeUrl();

            if (homeUrl == null || homeUrl.isEmpty()) {
                log.warn("Source {} has no home URL", source.getId());
                return contents;
            }

            // 抓取主页内容
            LinkScrapeResponse response = linkScrapeService.scrapeUrl(homeUrl, source.getUserId());

            if ("SUCCESS".equals(response.getStatus())) {
                CrawledContent content = new CrawledContent();
                content.setOriginalUrl(homeUrl);
                content.setPlatform(platform);
                content.setTitle(response.getTitle());
                content.setContent(response.getContent());
                content.setAuthor(response.getAuthor());
                content.setPublishedAt(response.getPublishTime());
                content.setCrawledAt(LocalDateTime.now());
                content.setStatus(CrawledContent.ContentStatus.RAW);
                content.setIsDuplicate(false);
                content.setIsSystemDiscovered(false);
                contents.add(content);
            }

        } catch (Exception e) {
            log.error("Error fetching content from source: {}", source.getId(), e);
        }

        return contents;
    }

    @Override
    public boolean testSourceAccessibility(String url, String platform) {
        try {
            LinkScrapeResponse response = linkScrapeService.scrapeUrl(url, null);
            return "SUCCESS".equals(response.getStatus());
        } catch (Exception e) {
            log.error("Source accessibility test failed: {}", url, e);
            return false;
        }
    }

    @Override
    public ContentCrawlJob getJobStatus(Long jobId) {
        return crawlJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Crawl job not found: " + jobId));
    }

    @Override
    @Transactional
    public void cancelJob(Long jobId) {
        ContentCrawlJob job = crawlJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Crawl job not found: " + jobId));

        if (job.getStatus() == ContentCrawlJob.JobStatus.RUNNING) {
            job.setStatus(ContentCrawlJob.JobStatus.CANCELLED);
            job.setCompletedAt(LocalDateTime.now());
            crawlJobRepository.save(job);
            log.info("Crawl job cancelled: {}", jobId);
        }
    }
}
