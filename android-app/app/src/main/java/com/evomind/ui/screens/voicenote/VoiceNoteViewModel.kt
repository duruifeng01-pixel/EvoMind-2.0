package com.evomind.ui.screens.voicenote

import android.content.Context
import android.media.MediaRecorder
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evomind.domain.model.*
import com.evomind.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

/**
 * 语音笔记ViewModel
 */
class VoiceNoteViewModel(
    private val voiceNoteUseCase: VoiceNoteUseCase,
    private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<VoiceNoteUiState>(VoiceNoteUiState.Idle)
    val uiState: StateFlow<VoiceNoteUiState> = _uiState.asStateFlow()

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _voiceNotes = MutableStateFlow<List<VoiceNote>>(emptyList())
    val voiceNotes: StateFlow<List<VoiceNote>> = _voiceNotes.asStateFlow()

    private val _statistics = MutableStateFlow<VoiceNoteStatistics?>(null)
    val statistics: StateFlow<VoiceNoteStatistics?> = _statistics.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTime: Long = 0

    // 录音时长（秒）
    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()

    /**
     * 开始录音
     */
    fun startRecording() {
        try {
            _recordingState.value = RecordingState.Recording
            recordingStartTime = System.currentTimeMillis()
            _recordingDuration.value = 0

            // 创建录音文件
            val outputFile = File(applicationContext.cacheDir, "voice_note_${System.currentTimeMillis()}.mp3")
            currentRecordingFile = outputFile

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            // 开始计时
            startRecordingTimer()

        } catch (e: IOException) {
            _recordingState.value = RecordingState.Error("录音启动失败: ${e.message}")
        }
    }

    /**
     * 停止录音并上传
     */
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            val duration = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
            _recordingDuration.value = duration

            _recordingState.value = RecordingState.Processing()

            // 上传录音
            uploadRecording(duration)

        } catch (e: Exception) {
            _recordingState.value = RecordingState.Error("录音停止失败: ${e.message}")
            cleanup()
        }
    }

    /**
     * 取消录音
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            cleanup()
            _recordingState.value = RecordingState.Idle
            _recordingDuration.value = 0

        } catch (e: Exception) {
            // 忽略取消时的错误
            cleanup()
        }
    }

    /**
     * 上传录音文件
     */
    private fun uploadRecording(duration: Int) {
        viewModelScope.launch {
            try {
                val file = currentRecordingFile ?: throw IllegalStateException("录音文件不存在")
                val audioBytes = file.readBytes()
                val audioBase64 = Base64.encodeToString(audioBytes, Base64.DEFAULT)

                voiceNoteUseCase.createVoiceNote(
                    audioBase64 = audioBase64,
                    duration = duration,
                    format = "mp3"
                ).collect { state ->
                    when (state) {
                        is VoiceNoteUiState.Loading -> {
                            _recordingState.value = RecordingState.Processing(50)
                        }
                        is VoiceNoteUiState.Success -> {
                            _recordingState.value = RecordingState.Success
                            _uiState.value = VoiceNoteUiState.NoteCreated(state.voiceNote)
                            // 刷新列表
                            loadVoiceNotes()
                        }
                        is VoiceNoteUiState.Error -> {
                            _recordingState.value = RecordingState.Error(state.message)
                        }
                    }
                }

            } catch (e: Exception) {
                _recordingState.value = RecordingState.Error("上传失败: ${e.message}")
            } finally {
                cleanup()
            }
        }
    }

    /**
     * 加载语音笔记列表
     */
    fun loadVoiceNotes() {
        viewModelScope.launch {
            voiceNoteUseCase.getVoiceNotes().collect { state ->
                when (state) {
                    is VoiceNoteListUiState.Success -> {
                        _voiceNotes.value = state.voiceNotes
                    }
                    is VoiceNoteListUiState.Error -> {
                        _uiState.value = VoiceNoteUiState.Error(state.message)
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 搜索语音笔记
     */
    fun searchVoiceNotes(keyword: String) {
        viewModelScope.launch {
            voiceNoteUseCase.searchVoiceNotes(keyword).collect { state ->
                when (state) {
                    is VoiceNoteListUiState.Success -> {
                        _voiceNotes.value = state.voiceNotes
                    }
                    is VoiceNoteListUiState.Error -> {
                        _uiState.value = VoiceNoteUiState.Error(state.message)
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 删除语音笔记
     */
    fun deleteVoiceNote(id: Long) {
        viewModelScope.launch {
            voiceNoteUseCase.deleteVoiceNote(id).collect { state ->
                when (state) {
                    is DeleteUiState.Success -> {
                        loadVoiceNotes()
                    }
                    is DeleteUiState.Error -> {
                        _uiState.value = VoiceNoteUiState.Error(state.message)
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 收藏/取消收藏
     */
    fun toggleFavorite(id: Long, favorite: Boolean) {
        viewModelScope.launch {
            voiceNoteUseCase.toggleFavorite(id, favorite).collect { state ->
                when (state) {
                    is VoiceNoteUiState.Success -> {
                        // 更新列表中的对应项
                        val updatedList = _voiceNotes.value.map {
                            if (it.id == id) state.voiceNote else it
                        }
                        _voiceNotes.value = updatedList
                    }
                    is VoiceNoteUiState.Error -> {
                        _uiState.value = VoiceNoteUiState.Error(state.message)
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 加载统计信息
     */
    fun loadStatistics() {
        viewModelScope.launch {
            voiceNoteUseCase.getStatistics().collect { state ->
                when (state) {
                    is StatisticsUiState.Success -> {
                        _statistics.value = state.statistics
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 开始录音计时
     */
    private fun startRecordingTimer() {
        viewModelScope.launch {
            while (_recordingState.value is RecordingState.Recording) {
                kotlinx.coroutines.delay(1000)
                if (_recordingState.value is RecordingState.Recording) {
                    _recordingDuration.value = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
                }
            }
        }
    }

    /**
     * 清理资源
     */
    private fun cleanup() {
        currentRecordingFile?.delete()
        currentRecordingFile = null
        mediaRecorder?.release()
        mediaRecorder = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}

/**
 * 语音笔记UI状态
 */
sealed class VoiceNoteUiState {
    object Idle : VoiceNoteUiState()
    data class NoteCreated(val voiceNote: VoiceNote) : VoiceNoteUiState()
    data class Error(val message: String) : VoiceNoteUiState()
}
