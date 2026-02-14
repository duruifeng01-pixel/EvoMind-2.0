package com.evomind.service;

import com.evomind.dto.request.LinkImportRequest;
import com.evomind.dto.response.LinkScrapeResponse;

/**
 * 链接抓取服务接口
 * 支持小红书、微信公众号、知乎、微博等平台链接抓取
 */
public interface LinkScrapeService {

    /**
     * 抓取链接内容
     *
     * @param url    链接URL
     * @param userId 用户ID（用于反爬策略）
     * @return 抓取结果
     */
    LinkScrapeResponse scrapeUrl(String url, Long userId);

    /**
     * 提交链接抓取任务（异步）
     *
     * @param userId  用户ID
     * @param request 链接导入请求
     * @return 任务响应
     */
    LinkScrapeResponse submitScrapeTask(Long userId, LinkImportRequest request);

    /**
     * 解析小红书链接
     *
     * @param url 小红书链接
     * @return 解析结果
     */
    LinkScrapeResponse scrapeXiaohongshu(String url);

    /**
     * 解析微信公众号链接
     *
     * @param url 微信文章链接
     * @return 解析结果
     */
    LinkScrapeResponse scrapeWechatArticle(String url);

    /**
     * 解析知乎链接
     *
     * @param url 知乎回答或文章链接
     * @return 解析结果
     */
    LinkScrapeResponse scrapeZhihu(String url);

    /**
     * 解析微博链接
     *
     * @param url 微博文章链接
     * @return 解析结果
     */
    LinkScrapeResponse scrapeWeibo(String url);

    /**
     * 获取抓取任务状态
     *
     * @param taskId 任务ID
     * @return 抓取结果
     */
    LinkScrapeResponse getTaskStatus(String taskId);

    /**
     * 检测链接平台类型
     *
     * @param url 链接URL
     * @return 平台类型
     */
    String detectPlatform(String url);

    /**
     * 验证链接是否可抓取
     *
     * @param url 链接URL
     * @return true表示可抓取
     */
    boolean isValidUrl(String url);
}
