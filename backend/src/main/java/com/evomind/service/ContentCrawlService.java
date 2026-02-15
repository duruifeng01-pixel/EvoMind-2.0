package com.evomind.service;

import com.evomind.entity.ContentCrawlJob;
import com.evomind.entity.CrawledContent;
import com.evomind.entity.Source;

import java.util.List;

/**
 * 内容采集服务接口
 * 负责从信息源抓取内容
 */
public interface ContentCrawlService {

    /**
     * 为指定信息源创建采集任务
     */
    ContentCrawlJob createCrawlJob(Long sourceId, Long userId);

    /**
     * 执行采集任务
     */
    ContentCrawlJob executeCrawlJob(Long jobId);

    /**
     * 批量执行采集任务
     */
    List<ContentCrawlJob> executeBatchCrawl(List<Long> sourceIds);

    /**
     * 从URL抓取内容
     */
    CrawledContent crawlContentFromUrl(String url, String platform);

    /**
     * 解析内容（提取正文）
     */
    String parseContent(String rawHtml, String platform);

    /**
     * 获取信息源的最新内容
     */
    List<CrawledContent> fetchLatestContent(Source source, int limit);

    /**
     * 测试信息源是否可访问
     */
    boolean testSourceAccessibility(String url, String platform);

    /**
     * 获取任务执行状态
     */
    ContentCrawlJob getJobStatus(Long jobId);

    /**
     * 取消正在执行的任务
     */
    void cancelJob(Long jobId);
}
