package com.evomind.data.repository

import com.evomind.data.model.LinkImportRequest
import com.evomind.data.model.LinkImportTask
import com.evomind.data.model.LinkScrapeResult
import kotlinx.coroutines.flow.Flow

/**
 * 链接导入仓库接口
 */
interface LinkImportRepository {
    /**
     * 提交链接抓取任务
     */
    suspend fun submitLinkImport(request: LinkImportRequest): Result<LinkImportTask>

    /**
     * 获取任务状态
     */
    suspend fun getTaskStatus(taskId: String): Result<LinkImportTask>

    /**
     * 获取用户的所有链接导入任务
     */
    fun getUserTasks(): Flow<List<LinkImportTask>>

    /**
     * 取消任务
     */
    suspend fun cancelTask(taskId: String): Result<Unit>

    /**
     * 重试失败的任务
     */
    suspend fun retryTask(taskId: String): Result<LinkImportTask>

    /**
     * 删除任务
     */
    suspend fun deleteTask(taskId: String): Result<Unit>

    /**
     * 清除已完成的任务
     */
    suspend fun clearCompletedTasks(): Result<Unit>
}
