package com.evomind.data.model

import java.time.LocalDateTime

/**
 * 链接导入请求
 */
data class LinkImportRequest(
    val url: String,
    val expectedPlatform: String? = null
)

/**
 * 链接抓取结果
 */
data class LinkScrapeResult(
    val success: Boolean,
    val url: String,
    val title: String? = null,
    val content: String? = null,
    val author: String? = null,
    val platform: String? = null,
    val images: List<ImageInfo> = emptyList(),
    val scrapedAt: LocalDateTime = LocalDateTime.now(),
    val errorMessage: String? = null
) {
    data class ImageInfo(
        val url: String,
        val description: String? = null
    )
}

/**
 * 链接导入任务状态
 */
enum class LinkImportStatus {
    PENDING,      // 等待处理
    SCRAPING,     // 抓取中
    SUCCESS,      // 抓取成功
    FAILED,       // 抓取失败
    RETRYING      // 重试中
}

/**
 * 链接导入任务
 */
data class LinkImportTask(
    val id: String,
    val url: String,
    val status: LinkImportStatus,
    val platform: String? = null,
    val result: LinkScrapeResult? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val errorMessage: String? = null
)

/**
 * 支持的平台类型
 */
sealed class PlatformType(val code: String, val displayName: String) {
    object XiaoHongShu : PlatformType("xiaohongshu", "小红书")
    object WeChat : PlatformType("weixin", "微信公众号")
    object Zhihu : PlatformType("zhihu", "知乎")
    object Weibo : PlatformType("weibo", "微博")
    object Unknown : PlatformType("unknown", "未知平台")

    companion object {
        fun fromUrl(url: String): PlatformType {
            return when {
                url.contains("xiaohongshu.com") || url.contains("xhslink.com") -> XiaoHongShu
                url.contains("mp.weixin.qq.com") || url.contains("weixin.qq.com") -> WeChat
                url.contains("zhihu.com") -> Zhihu
                url.contains("weibo.com") || url.contains("weibo.cn") -> Weibo
                else -> Unknown
            }
        }

        fun fromCode(code: String): PlatformType {
            return when (code) {
                "xiaohongshu" -> XiaoHongShu
                "weixin" -> WeChat
                "zhihu" -> Zhihu
                "weibo" -> Weibo
                else -> Unknown
            }
        }
    }
}
