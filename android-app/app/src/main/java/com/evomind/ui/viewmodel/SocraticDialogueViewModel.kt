package com.evomind.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.domain.model.*
import com.evomind.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 苏格拉底式对话ViewModel
 */
@HiltViewModel
class SocraticDialogueViewModel @Inject constructor(
    private val startDialogueUseCase: StartSocraticDialogueUseCase,
    private val sendMessageUseCase: SendSocraticMessageUseCase,
    private val getDialogueUseCase: GetSocraticDialogueUseCase,
    private val getMessagesUseCase: GetSocraticMessagesUseCase,
    private val finalizeDialogueUseCase: FinalizeSocraticDialogueUseCase,
    private val abandonDialogueUseCase: AbandonSocraticDialogueUseCase,
    private val getActiveDialogueUseCase: GetActiveSocraticDialogueUseCase,
    private val canStartDialogueUseCase: CanStartSocraticDialogueUseCase,
    private val regenerateResponseUseCase: RegenerateSocraticResponseUseCase
) : ViewModel() {

    // 当前对话
    private val _currentDialogue = mutableStateOf<SocraticDialogue?>(null)
    val currentDialogue: State<SocraticDialogue?> = _currentDialogue

    // 消息列表
    private val _messages = mutableStateListOf<SocraticMessage>()
    val messages: List<SocraticMessage> = _messages

    // UI状态
    private val _uiState = mutableStateOf<SocraticUiState>(SocraticUiState.Idle)
    val uiState: State<SocraticUiState> = _uiState

    // 洞察
    private val _insight = mutableStateOf<SocraticInsight?>(null)
    val insight: State<SocraticInsight?> = _insight

    // 输入文本
    private val _inputText = mutableStateOf("")
    val inputText: State<String> = _inputText

    // 是否可以发送
    private val _canSend = mutableStateOf(false)
    val canSend: State<Boolean> = _canSend

    fun updateInput(text: String) {
        _inputText.value = text
        _canSend.value = text.isNotBlank() && text.length <= SocraticDialogue.MAX_MESSAGE_LENGTH
    }

    /**
     * 检查讨论是否可以开始对话
     */
    fun checkCanStart(discussionId: Long, onResult: (Boolean, SocraticDialogue?) -> Unit) {
        viewModelScope.launch {
            // 先检查是否有活动对话
            getActiveDialogueUseCase(discussionId).onSuccess { activeDialogue ->
                if (activeDialogue != null) {
                    _currentDialogue.value = activeDialogue
                    loadMessages(activeDialogue.id)
                    onResult(false, activeDialogue)
                } else {
                    // 检查是否可以开始新对话
                    canStartDialogueUseCase(discussionId).onSuccess { canStart ->
                        onResult(canStart, null)
                    }.onFailure {
                        onResult(false, null)
                    }
                }
            }.onFailure {
                onResult(false, null)
            }
        }
    }

    /**
     * 开始新对话
     */
    fun startDialogue(discussionId: Long, initialThought: String? = null) {
        viewModelScope.launch {
            _uiState.value = SocraticUiState.Loading
            startDialogueUseCase(discussionId, initialThought).onSuccess { dialogue ->
                _currentDialogue.value = dialogue
                _messages.clear()
                dialogue.lastMessage?.let { _messages.add(it) }
                _uiState.value = SocraticUiState.Active
            }.onFailure { error ->
                _uiState.value = SocraticUiState.Error(error.message ?: "开始对话失败")
            }
        }
    }

    /**
     * 加载已有对话
     */
    fun loadDialogue(dialogueId: Long) {
        viewModelScope.launch {
            _uiState.value = SocraticUiState.Loading
            getDialogueUseCase(dialogueId).onSuccess { dialogue ->
                _currentDialogue.value = dialogue
                loadMessages(dialogueId)
                if (dialogue.isCompleted() || dialogue.isAbandoned()) {
                    _uiState.value = SocraticUiState.Completed
                } else {
                    _uiState.value = SocraticUiState.Active
                }
            }.onFailure { error ->
                _uiState.value = SocraticUiState.Error(error.message ?: "加载对话失败")
            }
        }
    }

    /**
     * 加载消息列表
     */
    private fun loadMessages(dialogueId: Long) {
        viewModelScope.launch {
            getMessagesUseCase(dialogueId).onSuccess { msgList ->
                _messages.clear()
                _messages.addAll(msgList)
            }
        }
    }

    /**
     * 发送用户消息
     */
    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _uiState.value !is SocraticUiState.Active) return

        val dialogueId = _currentDialogue.value?.id ?: return

        // 添加本地用户消息
        val tempMessage = SocraticMessage.createUserMessage(
            dialogueId = dialogueId,
            content = text,
            round = _currentDialogue.value?.currentRound ?: 0
        )
        _messages.add(tempMessage)
        _inputText.value = ""
        _canSend.value = false
        _uiState.value = SocraticUiState.Thinking

        viewModelScope.launch {
            sendMessageUseCase(dialogueId, text).onSuccess { aiResponse ->
                _messages.add(aiResponse)
                // 更新对话状态
                getDialogueUseCase(dialogueId).onSuccess { updatedDialogue ->
                    _currentDialogue.value = updatedDialogue
                    if (!updatedDialogue.canContinue) {
                        _uiState.value = SocraticUiState.ReadyToFinalize
                    } else {
                        _uiState.value = SocraticUiState.Active
                    }
                }
            }.onFailure { error ->
                _uiState.value = SocraticUiState.Error(error.message ?: "发送失败")
            }
        }
    }

    /**
     * 结束对话并生成洞察
     */
    fun finalizeDialogue(satisfaction: Int? = null) {
        val dialogueId = _currentDialogue.value?.id ?: return
        _uiState.value = SocraticUiState.GeneratingInsight

        viewModelScope.launch {
            finalizeDialogueUseCase(dialogueId, satisfaction).onSuccess { insight ->
                _insight.value = insight
                _uiState.value = SocraticUiState.InsightReady
            }.onFailure { error ->
                _uiState.value = SocraticUiState.Error(error.message ?: "生成洞察失败")
            }
        }
    }

    /**
     * 放弃对话
     */
    fun abandonDialogue(onComplete: () -> Unit = {}) {
        val dialogueId = _currentDialogue.value?.id ?: run {
            onComplete()
            return
        }

        viewModelScope.launch {
            abandonDialogueUseCase(dialogueId).onSuccess {
                _uiState.value = SocraticUiState.Abandoned
                onComplete()
            }.onFailure {
                onComplete()
            }
        }
    }

    /**
     * 重新生成AI回复
     */
    fun regenerateResponse(messageId: Long) {
        _uiState.value = SocraticUiState.Thinking
        viewModelScope.launch {
            regenerateResponseUseCase(messageId).onSuccess { newResponse ->
                // 替换最后一条AI消息
                val lastIndex = _messages.indexOfLast { it.isFromAi() }
                if (lastIndex >= 0) {
                    _messages[lastIndex] = newResponse
                }
                _uiState.value = SocraticUiState.Active
            }.onFailure { error ->
                _uiState.value = SocraticUiState.Error(error.message ?: "重新生成失败")
            }
        }
    }

    /**
     * 重置状态
     */
    fun reset() {
        _currentDialogue.value = null
        _messages.clear()
        _uiState.value = SocraticUiState.Idle
        _insight.value = null
        _inputText.value = ""
        _canSend.value = false
    }
}

/**
 * 苏格拉底式对话UI状态
 */
sealed class SocraticUiState {
    object Idle : SocraticUiState()
    object Loading : SocraticUiState()
    object Active : SocraticUiState() // 对话进行中
    object Thinking : SocraticUiState() // AI思考中
    object ReadyToFinalize : SocraticUiState() // 可以结束对话
    object GeneratingInsight : SocraticUiState() // 生成洞察中
    object InsightReady : SocraticUiState() // 洞察已生成
    object Completed : SocraticUiState() // 对话已完成
    object Abandoned : SocraticUiState() // 已放弃
    data class Error(val message: String) : SocraticUiState()
}
