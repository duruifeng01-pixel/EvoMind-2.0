package com.evomind.data.repository

import com.evomind.data.remote.api.PrivacyApi
import com.evomind.data.remote.dto.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 隐私与数据权利Repository
 */
@Singleton
class PrivacyRepository @Inject constructor(
    private val privacyApi: PrivacyApi
) {

    /**
     * 导出用户数据
     */
    fun exportUserData(): Flow<Result<Map<String, Any>>> = flow {
        try {
            val response = privacyApi.exportUserData()
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message ?: "导出失败")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * 申请注销账号
     */
    fun requestAccountDeletion(): Flow<Result<String>> = flow {
        try {
            val response = privacyApi.requestAccountDeletion()
            if (response.success && response.data != null) {
                val deletionToken = response.data["deletionToken"] as? String
                if (deletionToken != null) {
                    emit(Result.success(deletionToken))
                } else {
                    emit(Result.failure(Exception("获取注销令牌失败")))
                }
            } else {
                emit(Result.failure(Exception(response.message ?: "申请失败")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * 确认注销账号
     */
    fun confirmAccountDeletion(deletionToken: String): Flow<Result<Unit>> = flow {
        try {
            val response = privacyApi.confirmAccountDeletion(deletionToken)
            if (response.success) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception(response.message ?: "确认失败")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * 取消注销申请
     */
    fun cancelAccountDeletion(): Flow<Result<Unit>> = flow {
        try {
            val response = privacyApi.cancelAccountDeletion()
            if (response.success) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception(response.message ?: "取消失败")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * 获取导出历史
     */
    fun getExportHistory(): Flow<Result<Map<String, Any>>> = flow {
        try {
            val response = privacyApi.getExportHistory()
            if (response.success && response.data != null) {
                emit(Result.success(response.data))
            } else {
                emit(Result.failure(Exception(response.message ?: "获取历史失败")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
