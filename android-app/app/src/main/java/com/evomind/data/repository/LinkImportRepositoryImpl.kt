package com.evomind.data.repository

import com.evomind.data.model.LinkImportRequest
import com.evomind.data.model.LinkImportStatus
import com.evomind.data.model.LinkImportTask
import com.evomind.data.model.LinkScrapeResult
import com.evomind.data.remote.api.LinkImportRequestDto
import com.evomind.data.remote.api.OcrApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 链接导入仓库实现
 */
@Singleton
class LinkImportRepositoryImpl @Inject constructor(
    private val api: OcrApi
) : LinkImportRepository {

    // 本地任务缓存，用于模拟和快速响应
    private val _tasks = MutableStateFlow<List<LinkImportTask>>(emptyList())
    val tasks: Flow<List<LinkImportTask>> = _tasks.asStateFlow()

    override suspend fun submitLinkImport(request: LinkImportRequest): Result<LinkImportTask> {
        return try {
            // 创建新任务
            val task = LinkImportTask(
                id = UUID.randomUUID().toString(),
                url = request.url,
                status = LinkImportStatus.PENDING,
                platform = detectPlatform(request.url)
            )

            // 添加到本地缓存
            _tasks.update { it + task }

            // 调用 API 提交任务
            val requestDto = LinkImportRequestDto(
                url = request.url,
                expectedPlatform = request.expectedPlatform
            )
            val response = api.submitLinkImport(requestDto)
            if (response.isSuccessful) {
                val serverTaskId = response.body()?.taskId ?: task.id
                Result.success(task.copy(id = serverTaskId))
            } else {
                // API 调用失败，但仍返回本地任务用于展示
                Result.success(task)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTaskStatus(taskId: String): Result<LinkImportTask> {
        return try {
            val response = api.getLinkImportStatus(taskId)
            if (response.isSuccessful) {
                val task = response.body()?.toLinkImportTask()
                    ?: return Result.failure(IllegalStateException("Empty response"))
                
                // 更新本地缓存
                _tasks.update { tasks ->
                    tasks.map { if (it.id == taskId) task else it }
                }
                
                Result.success(task)
            } else {
                Result.failure(IllegalStateException("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserTasks(): Flow<List<LinkImportTask>> = tasks

    override suspend fun cancelTask(taskId: String): Result<Unit> {
        return try {
            val response = api.cancelLinkImport(taskId)
            if (response.isSuccessful) {
                // 更新本地状态
                _tasks.update { tasks ->
                    tasks.map { 
                        if (it.id == taskId) it.copy(status = LinkImportStatus.FAILED) 
                        else it 
                    }
                }
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Cancel failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun retryTask(taskId: String): Result<LinkImportTask> {
        return try {
            val currentTask = _tasks.value.find { it.id == taskId }
                ?: return Result.failure(IllegalStateException("Task not found"))

            // 重置任务状态
            val resetTask = currentTask.copy(
                status = LinkImportStatus.PENDING,
                errorMessage = null,
                completedAt = null
            )

            _tasks.update { tasks ->
                tasks.map { if (it.id == taskId) resetTask else it }
            }

            // 重新提交
            val response = api.retryLinkImport(taskId)
            if (response.isSuccessful) {
                Result.success(resetTask)
            } else {
                Result.failure(IllegalStateException("Retry failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            // 从本地缓存移除
            _tasks.update { tasks -> tasks.filter { it.id != taskId } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCompletedTasks(): Result<Unit> {
        return try {
            _tasks.update { tasks -> 
                tasks.filter { 
                    it.status != LinkImportStatus.SUCCESS && 
                    it.status != LinkImportStatus.FAILED 
                } 
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 根据 URL 检测平台类型
     */
    private fun detectPlatform(url: String): String {
        return when {
            url.contains("xiaohongshu.com") || url.contains("xhslink.com") -> "xiaohongshu"
            url.contains("mp.weixin.qq.com") || url.contains("weixin.qq.com") -> "weixin"
            url.contains("zhihu.com") -> "zhihu"
            url.contains("weibo.com") || url.contains("weibo.cn") -> "weibo"
            else -> "unknown"
        }
    }

    /**
     * 模拟链接抓取（用于开发测试）
     */
    suspend fun simulateScrape(taskId: String) {
        val task = _tasks.value.find { it.id == taskId } ?: return

        // 更新为抓取中
        _tasks.update { tasks ->
            tasks.map { 
                if (it.id == taskId) it.copy(status = LinkImportStatus.SCRAPING) 
                else it 
            }
        }

        // 模拟网络延迟
        delay(2000)

        // 模拟成功结果
        val result = com.evomind.data.model.LinkScrapeResult(
            success = true,
            url = task.url,
            title = "模拟抓取的文章标题",
            content = "这是模拟抓取的文章内容。在实际实现中，这里会包含从链接抓取到的完整内容...",
            author = "模拟作者",
            platform = task.platform,
            scrapedAt = LocalDateTime.now()
        )

        _tasks.update { tasks ->
            tasks.map { 
                if (it.id == taskId) {
                    it.copy(
                        status = LinkImportStatus.SUCCESS,
                        result = result,
                        completedAt = LocalDateTime.now()
                    )
                } else it 
            }
        }
    }
}
