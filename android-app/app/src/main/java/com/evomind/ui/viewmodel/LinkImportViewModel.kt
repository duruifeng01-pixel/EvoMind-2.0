package com.evomind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.data.model.LinkImportStatus
import com.evomind.data.model.LinkImportTask
import com.evomind.data.model.PlatformType
import com.evomind.domain.usecase.LinkImportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 链接导入页面状态
 */
data class LinkImportUiState(
    val url: String = "",
    val selectedPlatform: PlatformType? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentTask: LinkImportTask? = null,
    val tasks: List<LinkImportTask> = emptyList(),
    val supportedPlatforms: List<PlatformType> = emptyList()
)

/**
 * 链接导入 ViewModel
 */
@HiltViewModel
class LinkImportViewModel @Inject constructor(
    private val linkImportUseCase: LinkImportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkImportUiState())
    val uiState: StateFlow<LinkImportUiState> = _uiState.asStateFlow()

    init {
        // 加载支持的平台
        _uiState.update {
            it.copy(supportedPlatforms = linkImportUseCase.getSupportedPlatforms())
        }

        // 监听任务列表
        linkImportUseCase.getUserTasks()
            .onEach { tasks ->
                _uiState.update { it.copy(tasks = tasks) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * 更新链接输入
     */
    fun onUrlChange(url: String) {
        _uiState.update {
            val detectedPlatform = if (url.isNotBlank()) {
                linkImportUseCase.detectPlatform(url)
            } else null

            it.copy(
                url = url,
                selectedPlatform = detectedPlatform?.takeIf { p -> p != PlatformType.Unknown }
                    ?: it.selectedPlatform,
                errorMessage = null
            )
        }
    }

    /**
     * 选择平台
     */
    fun onPlatformSelect(platform: PlatformType?) {
        _uiState.update {
            it.copy(selectedPlatform = platform)
        }
    }

    /**
     * 提交链接导入
     */
    fun submitImport() {
        val currentState = _uiState.value
        val url = currentState.url.trim()

        if (url.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入链接") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            val result = linkImportUseCase.submitImport(
                url = url,
                expectedPlatform = currentState.selectedPlatform?.code
            )

            result.fold(
                onSuccess = { task ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentTask = task,
                            url = "",
                            successMessage = "任务已提交，正在抓取内容..."
                        )
                    }

                    // 开始轮询任务状态
                    pollTaskStatus(task.id)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "提交失败"
                        )
                    }
                }
            )
        }
    }

    /**
     * 轮询任务状态
     */
    private fun pollTaskStatus(taskId: String) {
        linkImportUseCase.pollTaskStatus(taskId)
            .onEach { task ->
                _uiState.update {
                    val message = when (task.status) {
                        LinkImportStatus.SUCCESS -> "抓取成功: ${task.result?.title ?: ""}"
                        LinkImportStatus.FAILED -> "抓取失败: ${task.errorMessage ?: ""}"
                        else -> it.successMessage
                    }

                    it.copy(
                        currentTask = task,
                        successMessage = message
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * 取消任务
     */
    fun cancelTask(taskId: String) {
        viewModelScope.launch {
            linkImportUseCase.cancelTask(taskId)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "取消失败")
                    }
                }
        }
    }

    /**
     * 重试任务
     */
    fun retryTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            linkImportUseCase.retryTask(taskId)
                .fold(
                    onSuccess = { task ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentTask = task,
                                successMessage = "任务已重新提交"
                            )
                        }
                        pollTaskStatus(task.id)
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "重试失败"
                            )
                        }
                    }
                )
        }
    }

    /**
     * 删除任务
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            linkImportUseCase.deleteTask(taskId)
        }
    }

    /**
     * 清除已完成的任务
     */
    fun clearCompletedTasks() {
        viewModelScope.launch {
            linkImportUseCase.clearCompletedTasks()
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 清除成功信息
     */
    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /**
     * 粘贴剪贴板内容
     */
    fun pasteUrl(url: String) {
        onUrlChange(url)
    }
}
