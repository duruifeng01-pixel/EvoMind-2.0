package com.evomind.service.impl;

import com.evomind.dto.request.LinkImportRequest;
import com.evomind.dto.response.LinkScrapeResponse;
import com.evomind.entity.ScrapedContent;
import com.evomind.repository.ScrapedContentRepository;
import com.evomind.service.LinkScrapeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 链接抓取服务实现
 * 使用Jsoup进行网页内容抓取
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkScrapeServiceImpl implements LinkScrapeService {

    private final ScrapedContentRepository scrapedContentRepository;
    private final ObjectMapper objectMapper;

    // 请求超时时间（毫秒）
    private static final int TIMEOUT_MS = 15000;
    // 请求延迟（毫秒）
    private static final int REQUEST_DELAY_MS = 1000;

    // User-Agent列表（轮换使用）
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1"
    };

    @Override
    @Transactional
    public LinkScrapeResponse scrapeUrl(String url, Long userId) {
        long startTime = System.currentTimeMillis();
        String taskId = generateTaskId();

        try {
            // 创建任务记录
            ScrapedContent content = new ScrapedContent();
            content.setTaskId(taskId);
            content.setUserId(userId);
            content.setSourceUrl(url);
            content.setPlatform(detectPlatform(url));
            content.setStatus(ScrapedContent.ScrapeStatus.SCRAPING);
            content.setCreatedAt(LocalDateTime.now());
            scrapedContentRepository.save(content);

            // 根据平台选择抓取策略
            LinkScrapeResponse result;
            switch (content.getPlatform()) {
                case "xiaohongshu":
                    result = scrapeXiaohongshu(url);
                    break;
                case "weixin":
                    result = scrapeWechatArticle(url);
                    break;
                case "zhihu":
                    result = scrapeZhihu(url);
                    break;
                case "weibo":
                    result = scrapeWeibo(url);
                    break;
                default:
                    result = scrapeGeneric(url);
            }

            // 更新任务状态
            content.setTitle(result.getTitle());
            content.setAuthor(result.getAuthor());
            content.setAuthorAvatar(result.getAuthorAvatar());
            content.setContent(result.getContent());
            content.setSummary(result.getSummary());
            content.setImagesJson(objectMapper.writeValueAsString(result.getImages()));
            content.setPublishTime(result.getPublishTime());
            content.setContentHash(calculateHash(result.getContent()));
            content.setWordCount(countWords(result.getContent()));
            content.setStatus(ScrapedContent.ScrapeStatus.SUCCESS);
            content.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            scrapedContentRepository.save(content);

            result.setTaskId(taskId);
            result.setScrapedAt(LocalDateTime.now());
            result.setProcessingTimeMs(content.getProcessingTimeMs());

            log.info("链接抓取完成: taskId={}, platform={}, title={}, 耗时{}ms",
                taskId, content.getPlatform(), result.getTitle(), content.getProcessingTimeMs());

            return result;

        } catch (Exception e) {
            log.error("链接抓取失败: url={}", url, e);

            // 更新失败状态
            scrapedContentRepository.findByTaskId(taskId).ifPresent(content -> {
                content.setStatus(ScrapedContent.ScrapeStatus.FAILED);
                content.setErrorMessage(e.getMessage());
                scrapedContentRepository.save(content);
            });

            return LinkScrapeResponse.builder()
                .taskId(taskId)
                .url(url)
                .status("FAILED")
                .errorMessage(e.getMessage())
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .build();
        }
    }

    @Override
    public LinkScrapeResponse submitScrapeTask(Long userId, LinkImportRequest request) {
        // 检查是否已存在相同URL的抓取记录
        var existing = scrapedContentRepository.findByUserIdAndSourceUrl(userId, request.getUrl());
        if (existing.isPresent() && existing.get().getStatus() == ScrapedContent.ScrapeStatus.SUCCESS) {
            log.info("URL已抓取过，返回缓存结果: url={}", request.getUrl());
            return getTaskStatus(existing.get().getTaskId());
        }

        // 开始新的抓取
        return scrapeUrl(request.getUrl(), userId);
    }

    @Override
    public LinkScrapeResponse scrapeXiaohongshu(String url) {
        try {
            // 延迟请求
            Thread.sleep(REQUEST_DELAY_MS);

            Document doc = Jsoup.connect(url)
                .userAgent(getRandomUserAgent())
                .timeout(TIMEOUT_MS)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .get();

            // 小红书笔记标题
            String title = doc.select("h1.title, div.title, .note-content h1").first() != null
                ? doc.select("h1.title, div.title, .note-content h1").first().text()
                : doc.title();

            // 作者信息
            String author = doc.select(".author-name, .nickname, .user-name").first() != null
                ? doc.select(".author-name, .nickname, .user-name").first().text()
                : "未知作者";

            // 正文内容
            String content = extractXiaohongshuContent(doc);

            // 图片
            List<LinkScrapeResponse.ImageInfo> images = extractImages(doc, ".note-content img, .content img, .main-content img");

            // 发布时间
            LocalDateTime publishTime = extractPublishTime(doc);

            return LinkScrapeResponse.builder()
                .url(url)
                .platform("xiaohongshu")
                .title(title)
                .author(author)
                .content(content)
                .summary(generateSummary(content, 200))
                .images(images)
                .publishTime(publishTime)
                .status("SUCCESS")
                .build();

        } catch (Exception e) {
            log.error("小红书抓取失败: url={}", url, e);
            return createErrorResponse(url, "xiaohongshu", e.getMessage());
        }
    }

    @Override
    public LinkScrapeResponse scrapeWechatArticle(String url) {
        try {
            Thread.sleep(REQUEST_DELAY_MS);

            Document doc = Jsoup.connect(url)
                .userAgent(getRandomUserAgent())
                .timeout(TIMEOUT_MS)
                .get();

            // 微信公众号文章标题
            String title = doc.select("h2.rich_media_title, #activity_name").first() != null
                ? doc.select("h2.rich_media_title, #activity_name").first().text().trim()
                : doc.title();

            // 公众号名称
            String author = doc.select("#js_name, .profile_nickname, #profileBt a").first() != null
                ? doc.select("#js_name, .profile_nickname, #profileBt a").first().text().trim()
                : "未知公众号";

            // 正文内容
            String content = extractWechatContent(doc);

            // 图片
            List<LinkScrapeResponse.ImageInfo> images = extractImages(doc, 
                "#js_content img, .rich_media_content img");

            // 发布时间
            LocalDateTime publishTime = extractWechatPublishTime(doc);

            return LinkScrapeResponse.builder()
                .url(url)
                .platform("weixin")
                .title(title)
                .author(author)
                .content(content)
                .summary(generateSummary(content, 200))
                .images(images)
                .publishTime(publishTime)
                .status("SUCCESS")
                .build();

        } catch (Exception e) {
            log.error("微信公众号抓取失败: url={}", url, e);
            return createErrorResponse(url, "weixin", e.getMessage());
        }
    }

    @Override
    public LinkScrapeResponse scrapeZhihu(String url) {
        try {
            Thread.sleep(REQUEST_DELAY_MS);

            Document doc = Jsoup.connect(url)
                .userAgent(getRandomUserAgent())
                .timeout(TIMEOUT_MS)
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .get();

            // 判断是文章还是回答
            boolean isArticle = url.contains("/p/") || url.contains("/zhuanlan/");

            String title, author, content;
            if (isArticle) {
                // 知乎文章
                title = doc.select("h1.Post-Title, h1.Title").first() != null
                    ? doc.select("h1.Post-Title, h1.Title").first().text()
                    : doc.title();
                author = doc.select(".AuthorInfo-name, .UserLink-link").first() != null
                    ? doc.select(".AuthorInfo-name, .UserLink-link").first().text()
                    : "未知作者";
                content = extractZhihuArticleContent(doc);
            } else {
                // 知乎回答
                title = doc.select("h1.QuestionHeader-title").first() != null
                    ? doc.select("h1.QuestionHeader-title").first().text()
                    : doc.title();
                author = doc.select(".AuthorInfo-name").first() != null
                    ? doc.select(".AuthorInfo-name").first().text()
                    : "未知作者";
                content = extractZhihuAnswerContent(doc);
            }

            // 图片
            List<LinkScrapeResponse.ImageInfo> images = extractImages(doc, 
                ".RichContent-inner img, .Post-RichTextContainer img, .ContentItem-RichText img");

            return LinkScrapeResponse.builder()
                .url(url)
                .platform("zhihu")
                .title(title)
                .author(author)
                .content(content)
                .summary(generateSummary(content, 200))
                .images(images)
                .status("SUCCESS")
                .build();

        } catch (Exception e) {
            log.error("知乎抓取失败: url={}", url, e);
            return createErrorResponse(url, "zhihu", e.getMessage());
        }
    }

    @Override
    public LinkScrapeResponse scrapeWeibo(String url) {
        // 微博文章抓取
        try {
            Document doc = fetchDocument(url);

            // 微博文章选择器
            Elements titleElements = doc.select(".title, .WB_title, .article-header h1, h1.woo-box-flex");
            Elements contentElements = doc.select(
                ".WB_text, .article-content, .detail_txt, .woo-box-item-flex .txt"
            );
            Elements authorElements = doc.select(
                ".WB_info a, .author-name, .user-name, .woo-box-flex .nickname"
            );

            String title = titleElements.first() != null
                ? titleElements.first().text().trim()
                : "微博内容";

            String content = contentElements.stream()
                .map(Element::text)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.joining("\n\n"));

            // 清理微博特殊标签
            content = cleanWeiboContent(content);

            String author = authorElements.first() != null
                ? authorElements.first().text().trim()
                : "微博用户";

            // 提取图片
            List<LinkScrapeResponse.ImageInfo> images = extractImages(doc);

            return LinkScrapeResponse.builder()
                .url(url)
                .platform("weibo")
                .title(title)
                .author(author)
                .content(content)
                .images(images)
                .status("SUCCESS")
                .build();

        } catch (Exception e) {
            log.error("抓取微博内容失败: {}", url, e);
            return createErrorResponse(url, "weibo", e.getMessage());
        }
    }

    /**
     * 清理微博内容
     */
    private String cleanWeiboContent(String content) {
        if (content == null) return "";

        // 移除微博话题标签的 ## 符号但保留文字
        content = content.replaceAll("#([^#]+)#", "$1");

        // 移除 @用户名 但保留用户名
        content = content.replaceAll("@([\\w\\u4e00-\\u9fa5]+)", "$1");

        // 移除 URL 缩短链接
        content = content.replaceAll("https?://t\\.cn/\\w+", "");

        // 移除 "展开" "收起" 等按钮文字
        content = content.replaceAll("展开$|收起$", "");

        // 移除多余空白
        content = content.replaceAll("\\s+", " ").trim();

        return content;
    }

    @Override
    public LinkScrapeResponse getTaskStatus(String taskId) {
        return scrapedContentRepository.findByTaskId(taskId)
            .map(content -> {
                try {
                    return LinkScrapeResponse.builder()
                        .taskId(content.getTaskId())
                        .url(content.getSourceUrl())
                        .platform(content.getPlatform())
                        .title(content.getTitle())
                        .author(content.getAuthor())
                        .content(content.getContent())
                        .summary(content.getSummary())
                        .images(objectMapper.readValue(
                            content.getImagesJson() != null ? content.getImagesJson() : "[]",
                            new TypeReference<List<LinkScrapeResponse.ImageInfo>>() {}
                        ))
                        .publishTime(content.getPublishTime())
                        .status(content.getStatus().name())
                        .errorMessage(content.getErrorMessage())
                        .processingTimeMs(content.getProcessingTimeMs())
                        .build();
                } catch (Exception e) {
                    log.error("解析抓取结果失败", e);
                    return LinkScrapeResponse.builder()
                        .taskId(taskId)
                        .status("ERROR")
                        .errorMessage("解析结果失败")
                        .build();
                }
            })
            .orElse(LinkScrapeResponse.builder()
                .taskId(taskId)
                .status("NOT_FOUND")
                .errorMessage("任务不存在")
                .build());
    }

    @Override
    public String detectPlatform(String url) {
        if (url.contains("xiaohongshu.com") || url.contains("xhslink.com")) {
            return "xiaohongshu";
        } else if (url.contains("mp.weixin.qq.com")) {
            return "weixin";
        } else if (url.contains("zhihu.com")) {
            return "zhihu";
        } else if (url.contains("weibo.com") || url.contains("weibo.cn")) {
            return "weibo";
        }
        return "unknown";
    }

    @Override
    public boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            new URL(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }

    // ============ 私有辅助方法 ============

    private LinkScrapeResponse scrapeGeneric(String url) {
        try {
            Thread.sleep(REQUEST_DELAY_MS);

            Document doc = Jsoup.connect(url)
                .userAgent(getRandomUserAgent())
                .timeout(TIMEOUT_MS)
                .get();

            String title = doc.title();
            String content = doc.body().text();

            return LinkScrapeResponse.builder()
                .url(url)
                .platform("unknown")
                .title(title)
                .content(content)
                .summary(generateSummary(content, 200))
                .status("SUCCESS")
                .build();

        } catch (Exception e) {
            return createErrorResponse(url, "unknown", e.getMessage());
        }
    }

    private String extractXiaohongshuContent(Document doc) {
        // 尝试多种选择器
        Elements contentElements = doc.select(
            ".note-content .content, .desc, .main-content .text, .note-desc"
        );
        
        if (!contentElements.isEmpty()) {
            return contentElements.first().html();
        }
        
        // 兜底：提取所有文本段落
        StringBuilder content = new StringBuilder();
        doc.select("p, div.content, span.desc").forEach(el -> {
            if (el.text().length() > 10) {
                content.append("<p>").append(el.text()).append("</p>");
            }
        });
        return content.toString();
    }

    private String extractWechatContent(Document doc) {
        Element contentElement = doc.select("#js_content, .rich_media_content").first();
        if (contentElement != null) {
            // 清理微信特有的属性
            contentElement.select("script, style").remove();
            return contentElement.html();
        }
        return "";
    }

    private String extractZhihuArticleContent(Document doc) {
        Element contentElement = doc.select(".Post-RichTextContainer, .RichText").first();
        return contentElement != null ? contentElement.html() : "";
    }

    private String extractZhihuAnswerContent(Document doc) {
        Element contentElement = doc.select(".RichContent-inner, .RichContent").first();
        return contentElement != null ? contentElement.html() : "";
    }

    private List<LinkScrapeResponse.ImageInfo> extractImages(Document doc, String selector) {
        List<LinkScrapeResponse.ImageInfo> images = new ArrayList<>();
        Elements imgElements = doc.select(selector);
        
        for (Element img : imgElements) {
            String src = img.attr("data-src");
            if (src.isEmpty()) {
                src = img.attr("src");
            }
            if (!src.isEmpty() && (src.startsWith("http") || src.startsWith("//"))) {
                images.add(LinkScrapeResponse.ImageInfo.builder()
                    .url(src.startsWith("//") ? "https:" + src : src)
                    .description(img.attr("alt"))
                    .downloaded(false)
                    .build());
            }
        }
        
        return images;
    }

    private LocalDateTime extractPublishTime(Document doc) {
        // 尝试从meta标签或页面元素提取时间
        Elements timeElements = doc.select("time, .publish-time, .time, [datetime]");
        if (!timeElements.isEmpty()) {
            String timeStr = timeElements.first().attr("datetime");
            if (timeStr.isEmpty()) {
                timeStr = timeElements.first().text();
            }
            // 简化处理，实际需要更完善的日期解析
            try {
                return LocalDateTime.now(); // 占位
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private LocalDateTime extractWechatPublishTime(Document doc) {
        Element timeElement = doc.select("#publish_time, .publish_time, #post-date").first();
        if (timeElement != null) {
            // 解析微信时间格式
            return LocalDateTime.now(); // 占位
        }
        return null;
    }

    private String generateSummary(String content, int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // 去除HTML标签
        String plainText = Jsoup.parse(content).text();
        if (plainText.length() <= maxLength) {
            return plainText;
        }
        return plainText.substring(0, maxLength) + "...";
    }

    private String calculateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(content != null ? content.getBytes() : new byte[0]);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    private int countWords(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        // 去除HTML标签后统计
        String plainText = Jsoup.parse(content).text();
        return plainText.length();
    }

    private String getRandomUserAgent() {
        return USER_AGENTS[(int) (Math.random() * USER_AGENTS.length)];
    }

    private String generateTaskId() {
        return "link_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private LinkScrapeResponse createErrorResponse(String url, String platform, String errorMessage) {
        return LinkScrapeResponse.builder()
            .url(url)
            .platform(platform)
            .status("FAILED")
            .errorMessage(errorMessage)
            .build();
    }
}
