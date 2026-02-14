package com.evomind.domain.repository

import com.evomind.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * OCR识别Repository接口
 */
interface OcrRepository {
    
    /**
     * 上传图片进行OCR识别
     * @param request OCR导入请求
     * @return 识别结果
     */
    suspend fun recognizeBloggers(request: OcrImportRequest): Result<OcrResult>
    
    /**
     * 根据任务ID获取识别结果
     * @param taskId 任务ID
     * @return 识别结果
     */
    suspend fun getResultByTaskId(taskId: String): Result<OcrResult>
    
    /**
     * 确认导入选中的博主
     * @param request 确认导入请求
     * @return 导入结果列表
     */
    suspend fun confirmImport(request: ConfirmImportRequest): Result<List<ImportResult>>
    
    /**
     * 获取今日导入限额信息
     * @return 限额信息
     */
    suspend fun getImportLimit(): Result<ImportLimit>
    
    /**
     * 将图片转换为Base64
     * @param imageBytes 图片字节数组
     * @return Base64编码字符串
     */
    fun encodeImageToBase64(imageBytes: ByteArray): String
    
    /**
     * 压缩图片到指定大小以下
     * @param imageBytes 原始图片字节
     * @param maxSizeKB 最大大小(KB)
     * @return 压缩后的字节数组
     */
    suspend fun compressImage(imageBytes: ByteArray, maxSizeKB: Int = 1024): ByteArray
}
