package com.evomind.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.evomind.data.remote.api.*
import com.evomind.domain.model.*
import com.evomind.domain.repository.OcrRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * OCR Repository实现
 */
class OcrRepositoryImpl(
    private val ocrApi: OcrApi
) : OcrRepository {

    override suspend fun recognizeBloggers(request: OcrImportRequest): Result<OcrResult> =
        withContext(Dispatchers.IO) {
            try {
                val dto = OcrImportRequestDto(
                    imageBase64 = request.imageBase64,
                    imageFormat = request.imageFormat,
                    platform = request.platform,
                    preprocessImage = request.preprocessImage,
                    imageWidth = request.imageWidth,
                    imageHeight = request.imageHeight
                )

                val response = ocrApi.ocrImport(dto)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(mapToOcrResult(body.data))
                    } else {
                        Result.failure(Exception(body?.message ?: "识别失败"))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getResultByTaskId(taskId: String): Result<OcrResult> =
        withContext(Dispatchers.IO) {
            try {
                val response = ocrApi.getOcrResult(taskId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(mapToOcrResult(body.data))
                    } else {
                        Result.failure(Exception(body?.message ?: "获取结果失败"))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun confirmImport(request: ConfirmImportRequest): Result<List<ImportResult>> =
        withContext(Dispatchers.IO) {
            try {
                val dto = ConfirmImportRequestDto(
                    taskId = request.taskId,
                    selectedCandidateIds = request.selectedCandidateIds,
                    selectAll = request.selectAll,
                    customNames = request.customNames
                )

                val response = ocrApi.confirmImport(dto)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(body.data.map { mapToImportResult(it) })
                    } else {
                        Result.failure(Exception(body?.message ?: "导入失败"))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getImportLimit(): Result<ImportLimit> =
        withContext(Dispatchers.IO) {
            try {
                val response = ocrApi.getImportLimit()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Result.success(
                            ImportLimit(
                                dailyLimit = body.data.dailyLimit,
                                isExceeded = body.data.isExceeded,
                                remaining = body.data.remaining
                            )
                        )
                    } else {
                        Result.failure(Exception(body?.message ?: "获取限额失败"))
                    }
                } else {
                    Result.failure(Exception("网络请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun encodeImageToBase64(imageBytes: ByteArray): String {
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }

    override suspend fun compressImage(imageBytes: ByteArray, maxSizeKB: Int): ByteArray =
        withContext(Dispatchers.IO) {
            val maxSizeBytes = maxSizeKB * 1024

            // 如果已经小于限制，直接返回
            if (imageBytes.size <= maxSizeBytes) {
                return@withContext imageBytes
            }

            // 解码图片
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return@withContext imageBytes

            var quality = 90
            var compressedBytes: ByteArray

            val outputStream = ByteArrayOutputStream()

            do {
                outputStream.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedBytes = outputStream.toByteArray()
                quality -= 10
            } while (compressedBytes.size > maxSizeBytes && quality > 10)

            // 如果压缩后仍然太大，缩放尺寸
            if (compressedBytes.size > maxSizeBytes) {
                val scaleFactor = 0.8f
                val newWidth = (bitmap.width * scaleFactor).toInt()
                val newHeight = (bitmap.height * scaleFactor).toInt()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

                outputStream.reset()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                compressedBytes = outputStream.toByteArray()

                scaledBitmap.recycle()
            }

            bitmap.recycle()
            outputStream.close()

            compressedBytes
        }

    private fun mapToOcrResult(dto: OcrResultDto): OcrResult {
        return OcrResult(
            taskId = dto.taskId,
            bloggers = dto.bloggers?.map { mapToDetectedBlogger(it) } ?: emptyList(),
            textBlocks = dto.textBlocks?.map { mapToTextBlock(it) } ?: emptyList(),
            status = dto.status,
            processingTimeMs = dto.processingTimeMs,
            recognizedAt = dto.recognizedAt?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
            },
            needsConfirmation = dto.needsConfirmation ?: true
        )
    }

    private fun mapToDetectedBlogger(dto: DetectedBloggerDto): DetectedBlogger {
        return DetectedBlogger(
            candidateId = dto.candidateId,
            name = dto.name,
            avatarUrl = dto.avatarUrl,
            platform = dto.platform,
            homeUrl = dto.homeUrl,
            confidence = dto.confidence,
            alreadyExists = dto.alreadyExists ?: false,
            existingSourceId = dto.existingSourceId,
            boundingBox = dto.boundingBox?.let { mapToBoundingBox(it) }
        )
    }

    private fun mapToTextBlock(dto: TextBlockDto): TextBlock {
        return TextBlock(
            text = dto.text,
            boundingBox = dto.boundingBox?.let { mapToBoundingBox(it) },
            confidence = dto.confidence
        )
    }

    private fun mapToBoundingBox(dto: BoundingBoxDto): BoundingBox {
        return BoundingBox(
            x = dto.x,
            y = dto.y,
            width = dto.width,
            height = dto.height
        )
    }

    private fun mapToImportResult(dto: ImportResultDto): ImportResult {
        return ImportResult(
            sourceId = dto.sourceId,
            name = dto.name,
            platform = dto.platform,
            existed = dto.existed ?: false
        )
    }
}
