package com.evomind.domain.usecase

import com.evomind.data.model.LinkImportRequest
import com.evomind.data.model.LinkImportStatus
import com.evomind.data.model.LinkImportTask
import com.evomind.data.model.PlatformType
import com.evomind.data.repository.LinkImportRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * 链接导入用例
 */
class LinkImportUseCase @Inject constructor(
    private val repository: LinkImportRepository
) {
    /**
     * 提交链接导入任务
     */
    suspend fun submitImport(url: String, expectedPlatform: String? = null): Result<LinkImportTask> {
        // 验证 URL 格式
        if (!isValidUrl(url)) {
            return Result.failure(IllegalArgumentException("无效的链接格式"))
        }

        // 验证平台匹配
        if (expectedPlatform != null) {
            val detectedPlatform = PlatformType.fromUrl(url)
            if (detectedPlatform.code != expectedPlatform) {
                return Result.failure(
                    IllegalArgumentException(
                        "链接平台与期望不匹配，检测到: ${detectedPlatform.displayName}"
                    )
                )
            }
        }

        val request = LinkImportRequest(
            url = url.trim(),
            expectedPlatform = expectedPlatform
        )

        return repository.submitLinkImport(request)
    }

    /**
     * 获取任务状态流（自动轮询）
     */
    fun pollTaskStatus(taskId: String, intervalMs: Long = 2000): Flow<LinkImportTask> = flow {
        while (true) {
            val result = repository.getTaskStatus(taskId)
            if (result.isSuccess) {
                val task = result.getOrThrow()
                emit(task)

                // 如果任务已完成或失败，停止轮询
                if (task.status == LinkImportStatus.SUCCESS ||
                    task.status == LinkImportStatus.FAILED
                ) {
                    break
                }
            } else {
                // 获取失败时继续轮询
                delay(intervalMs)
                continue
            }

            delay(intervalMs)
        }
    }

    /**
     * 获取用户的所有任务
     */
    fun getUserTasks(): Flow<List<LinkImportTask>> = repository.getUserTasks()

    /**
     * 取消任务
     */
    suspend fun cancelTask(taskId: String): Result<Unit> {
        return repository.cancelTask(taskId)
    }

    /**
     * 重试失败的任务
     */
    suspend fun retryTask(taskId: String): Result<LinkImportTask> {
        return repository.retryTask(taskId)
    }

    /**
     * 删除任务
     */
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return repository.deleteTask(taskId)
    }

    /**
     * 清除已完成的任务
     */
    suspend fun clearCompletedTasks(): Result<Unit> {
        return repository.clearCompletedTasks()
    }

    /**
     * 验证 URL 格式
     */
    private fun isValidUrl(url: String): Boolean {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) return false

        // 基本 URL 验证
        val urlPattern = (
            "^(https?://)?" + // protocol
            "([a-zA-Z0-9][-a-zA-Z0-9]*\\.)*" + // subdomains
            "[a-zA-Z0-9][-a-zA-Z0-9]{0,62}" + // domain
            "\\.[a-zA-Z]{2,}" + // TLD
            "(/[^\\s]*)?$"
        ).toRegex()

        if (!urlPattern.matches(trimmedUrl)) return false

        // 检查是否支持的平台
        val platform = PlatformType.fromUrl(trimmedUrl)
        return platform != PlatformType.Unknown
    }

    /**
     * 从 URL 中提取平台类型
     */
    fun detectPlatform(url: String): PlatformType {
        return PlatformType.fromUrl(url)
    }

    /**
     * 获取平台显示名称
     */
    fun getPlatformDisplayName(code: String): String {
        return PlatformType.fromCode(code).displayName
    }

    /**
     * 获取所有支持的平台
     */
    fun getSupportedPlatforms(): List<PlatformType> {
        return listOf(
            PlatformType.XiaoHongShu,
            PlatformType.WeChat,
            PlatformType.Zhihu,
            PlatformType.Douyin
        )
    }
}
