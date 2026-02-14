package com.evomind.service;

import com.evomind.dto.request.VoiceNoteRequest;
import com.evomind.dto.response.VoiceNoteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 语音笔记服务接口
 */
public interface VoiceNoteService {

    /**
     * 创建语音笔记
     * 上传音频文件并自动转写
     *
     * @param userId    用户ID
     * @param audioFile 音频文件
     * @param request   请求参数
     * @return 创建的语音笔记
     */
    VoiceNoteResponse createVoiceNote(Long userId, MultipartFile audioFile, VoiceNoteRequest request);

    /**
     * 从Base64音频创建语音笔记
     *
     * @param userId      用户ID
     * @param audioBase64 Base64编码的音频
     * @param format      音频格式
     * @param duration    音频时长（秒）
     * @param request     请求参数
     * @return 创建的语音笔记
     */
    VoiceNoteResponse createFromBase64(Long userId, String audioBase64, String format, 
                                        Integer duration, VoiceNoteRequest request);

    /**
     * 获取语音笔记详情
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 语音笔记详情
     */
    VoiceNoteResponse getVoiceNote(Long userId, Long noteId);

    /**
     * 获取用户的语音笔记列表
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 语音笔记分页列表
     */
    Page<VoiceNoteResponse> getUserVoiceNotes(Long userId, Pageable pageable);

    /**
     * 搜索语音笔记
     *
     * @param userId  用户ID
     * @param keyword 关键词
     * @param pageable 分页参数
     * @return 搜索结果
     */
    Page<VoiceNoteResponse> searchVoiceNotes(Long userId, String keyword, Pageable pageable);

    /**
     * 更新语音笔记
     *
     * @param userId  用户ID
     * @param noteId  笔记ID
     * @param request 更新请求
     * @return 更新后的语音笔记
     */
    VoiceNoteResponse updateVoiceNote(Long userId, Long noteId, VoiceNoteRequest request);

    /**
     * 删除语音笔记
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     */
    void deleteVoiceNote(Long userId, Long noteId);

    /**
     * 收藏/取消收藏语音笔记
     *
     * @param userId   用户ID
     * @param noteId   笔记ID
     * @param favorite 是否收藏
     * @return 更新后的语音笔记
     */
    VoiceNoteResponse toggleFavorite(Long userId, Long noteId, boolean favorite);

    /**
     * 归档语音笔记
     *
     * @param userId  用户ID
     * @param noteId  笔记ID
     * @param archive 是否归档
     * @return 更新后的语音笔记
     */
    VoiceNoteResponse toggleArchive(Long userId, Long noteId, boolean archive);

    /**
     * 重新转写语音笔记
     *
     * @param userId 用户ID
     * @param noteId 笔记ID
     * @return 更新后的语音笔记
     */
    VoiceNoteResponse retranscribe(Long userId, Long noteId);

    /**
     * 获取用户收藏的语音笔记
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 收藏的语音笔记列表
     */
    Page<VoiceNoteResponse> getFavoriteVoiceNotes(Long userId, Pageable pageable);

    /**
     * 获取用户语音笔记统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    VoiceNoteStatistics getStatistics(Long userId);

    /**
     * 语音笔记统计信息
     */
    record VoiceNoteStatistics(
        Long totalCount,
        Long todayCount,
        Integer totalDurationSeconds,
        Long pendingTranscribeCount
    ) {}
}
