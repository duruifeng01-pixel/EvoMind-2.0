package com.evomind.service.impl;

import com.evomind.dto.request.VoiceNoteRequest;
import com.evomind.dto.response.VoiceNoteResponse;
import com.evomind.entity.VoiceNote;
import com.evomind.exception.BusinessException;
import com.evomind.repository.VoiceNoteRepository;
import com.evomind.service.VoiceNoteService;
import com.evomind.service.VoiceTranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * 语音笔记服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceNoteServiceImpl implements VoiceNoteService {

    private final VoiceNoteRepository voiceNoteRepository;
    private final VoiceTranscriptionService transcriptionService;

    @Override
    @Transactional
    public VoiceNoteResponse createVoiceNote(Long userId, MultipartFile audioFile, VoiceNoteRequest request) {
        try {
            log.info("创建语音笔记: userId={}", userId);

            // 创建语音笔记实体
            VoiceNote note = new VoiceNote();
            note.setUserId(userId);
            note.setAudioFormat(getFileExtension(audioFile.getOriginalFilename()));
            note.setFileSizeBytes(audioFile.getSize());
            note.setTranscribeStatus("PENDING");
            note.setIsFavorite(Optional.ofNullable(request.getIsFavorite()).orElse(false));
            note.setIsArchived(Optional.ofNullable(request.getIsArchived()).orElse(false));
            note.setTitle(request.getTitle());
            note.setTags(request.getTags());

            // 先保存获取ID
            VoiceNote savedNote = voiceNoteRepository.save(note);

            // 执行转写
            transcribeAudioAsync(savedNote, audioFile);

            log.info("语音笔记创建成功: noteId={}", savedNote.getId());
            return convertToResponse(savedNote);

        } catch (Exception e) {
            log.error("创建语音笔记失败", e);
            throw new BusinessException("创建语音笔记失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public VoiceNoteResponse createFromBase64(Long userId, String audioBase64, String format,
                                               Integer duration, VoiceNoteRequest request) {
        try {
            log.info("从Base64创建语音笔记: userId={}, format={}, duration={}", userId, format, duration);

            // 解码计算文件大小
            byte[] audioData = Base64.getDecoder().decode(audioBase64);

            // 创建语音笔记实体
            VoiceNote note = new VoiceNote();
            note.setUserId(userId);
            note.setAudioFormat(format != null ? format : "pcm");
            note.setAudioDurationSeconds(duration);
            note.setFileSizeBytes((long) audioData.length);
            note.setTranscribeStatus("PROCESSING");
            note.setIsFavorite(Optional.ofNullable(request.getIsFavorite()).orElse(false));
            note.setIsArchived(Optional.ofNullable(request.getIsArchived()).orElse(false));
            note.setTitle(request.getTitle());
            note.setTags(request.getTags());

            VoiceNote savedNote = voiceNoteRepository.save(note);

            // 执行转写
            String transcribedText = transcriptionService.transcribeFromBase64(
                    audioBase64, 
                    format != null ? format : "pcm", 
                    16000
            );

            // 更新转写结果
            savedNote.setTranscribedText(transcribedText);
            savedNote.setTranscribeStatus("SUCCESS");
            savedNote.setTranscribedAt(LocalDateTime.now());
            
            // 如果没有标题，使用前20个字符作为标题
            if (!StringUtils.hasText(savedNote.getTitle()) && StringUtils.hasText(transcribedText)) {
                String autoTitle = transcribedText.length() > 20 
                    ? transcribedText.substring(0, 20) + "..." 
                    : transcribedText;
                savedNote.setTitle(autoTitle);
            }

            VoiceNote updatedNote = voiceNoteRepository.save(savedNote);

            log.info("语音笔记创建并转写成功: noteId={}", updatedNote.getId());
            return convertToResponse(updatedNote);

        } catch (Exception e) {
            log.error("从Base64创建语音笔记失败", e);
            throw new BusinessException("创建语音笔记失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public VoiceNoteResponse getVoiceNote(Long userId, Long noteId) {
        VoiceNote note = voiceNoteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new BusinessException("语音笔记不存在"));
        return convertToResponse(note);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoiceNoteResponse> getUserVoiceNotes(Long userId, Pageable pageable) {
        return voiceNoteRepository.findByUserIdOrderByRecordedAtDesc(userId, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoiceNoteResponse> searchVoiceNotes(Long userId, String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return getUserVoiceNotes(userId, pageable);
        }
        return voiceNoteRepository.searchByKeyword(userId, keyword, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional
    public VoiceNoteResponse updateVoiceNote(Long userId, Long noteId, VoiceNoteRequest request) {
        VoiceNote note = voiceNoteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new BusinessException("语音笔记不存在"));

        if (StringUtils.hasText(request.getTitle())) {
            note.setTitle(request.getTitle());
        }
        if (request.getTags() != null) {
            note.setTags(request.getTags());
        }
        if (StringUtils.hasText(request.getTranscribedText())) {
            note.setTranscribedText(request.getTranscribedText());
        }
        if (request.getIsFavorite() != null) {
            note.setIsFavorite(request.getIsFavorite());
        }
        if (request.getIsArchived() != null) {
            note.setIsArchived(request.getIsArchived());
        }

        VoiceNote updatedNote = voiceNoteRepository.save(note);
        return convertToResponse(updatedNote);
    }

    @Override
    @Transactional
    public void deleteVoiceNote(Long userId, Long noteId) {
        VoiceNote note = voiceNoteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new BusinessException("语音笔记不存在"));
        voiceNoteRepository.delete(note);
        log.info("删除语音笔记: noteId={}", noteId);
    }

    @Override
    @Transactional
    public VoiceNoteResponse toggleFavorite(Long userId, Long noteId, boolean favorite) {
        VoiceNote note = voiceNoteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new BusinessException("语音笔记不存在"));
        note.setIsFavorite(favorite);
        return convertToResponse(voiceNoteRepository.save(note));
    }

    @Override
    @Transactional
    public VoiceNoteResponse toggleArchive(Long userId, Long noteId, boolean archive) {
        VoiceNote note = voiceNoteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new BusinessException("语音笔记不存在"));
        note.setIsArchived(archive);
        return convertToResponse(voiceNoteRepository.save(note));
    }

    @Override
    @Transactional
    public VoiceNoteResponse retranscribe(Long userId, Long noteId) {
        VoiceNote note = voiceNoteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new BusinessException("语音笔记不存在"));

        // 目前只支持重新转写失败的记录
        if (!"FAILED".equals(note.getTranscribeStatus()) && !"PENDING".equals(note.getTranscribeStatus())) {
            throw new BusinessException("当前状态不支持重新转写");
        }

        note.setTranscribeStatus("PENDING");
        note.setTranscribeError(null);
        VoiceNote savedNote = voiceNoteRepository.save(note);

        // TODO: 如果有音频文件，重新触发转写
        
        return convertToResponse(savedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoiceNoteResponse> getFavoriteVoiceNotes(Long userId, Pageable pageable) {
        return voiceNoteRepository.findByUserIdAndIsFavoriteTrueOrderByRecordedAtDesc(userId, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VoiceNoteStatistics getStatistics(Long userId) {
        // 今日统计
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayCount = voiceNoteRepository.countByUserIdAndRecordedAtBetween(userId, todayStart, todayEnd);

        // 总数量
        Long totalCount = (long) voiceNoteRepository.findByUserIdAndIsArchivedFalseOrderByRecordedAtDesc(userId).size();

        // 总时长
        Integer totalDuration = voiceNoteRepository.sumAudioDurationByUserId(userId);

        // 待转写数量
        List<VoiceNote> pendingList = voiceNoteRepository.findByUserIdAndTranscribeStatusOrderByRecordedAtAsc(
                userId, "PENDING");
        Long pendingCount = (long) pendingList.size();

        return new VoiceNoteStatistics(totalCount, todayCount, totalDuration, pendingCount);
    }

    /**
     * 异步转写音频（简单实现，实际可以用线程池或消息队列）
     */
    private void transcribeAudioAsync(VoiceNote note, MultipartFile audioFile) {
        // 简化实现，实际应该使用异步任务
        new Thread(() -> {
            try {
                note.setTranscribeStatus("PROCESSING");
                voiceNoteRepository.save(note);

                String result = transcriptionService.transcribeSync(audioFile);

                note.setTranscribedText(result);
                note.setTranscribeStatus("SUCCESS");
                note.setTranscribedAt(LocalDateTime.now());

                // 自动生成标题
                if (!StringUtils.hasText(note.getTitle()) && StringUtils.hasText(result)) {
                    String autoTitle = result.length() > 20 
                        ? result.substring(0, 20) + "..." 
                        : result;
                    note.setTitle(autoTitle);
                }

                voiceNoteRepository.save(note);
                log.info("语音转写完成: noteId={}", note.getId());

            } catch (Exception e) {
                log.error("语音转写失败: noteId={}", note.getId(), e);
                note.setTranscribeStatus("FAILED");
                note.setTranscribeError(e.getMessage());
                voiceNoteRepository.save(note);
            }
        }).start();
    }

    /**
     * 转换实体为响应DTO
     */
    private VoiceNoteResponse convertToResponse(VoiceNote note) {
        VoiceNoteResponse response = new VoiceNoteResponse();
        response.setId(note.getId());
        response.setTitle(note.getTitle());
        response.setAudioUrl(note.getAudioUrl());
        response.setAudioDurationSeconds(note.getAudioDurationSeconds());
        response.setAudioFormat(note.getAudioFormat());
        response.setFileSizeBytes(note.getFileSizeBytes());
        response.setTranscribedText(note.getTranscribedText());
        response.setTranscribeStatus(note.getTranscribeStatus());
        response.setTranscribeError(note.getTranscribeError());
        response.setTags(note.getTags());
        response.setIsFavorite(note.getIsFavorite());
        response.setIsArchived(note.getIsArchived());
        response.setRecordedAt(note.getRecordedAt());
        response.setTranscribedAt(note.getTranscribedAt());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());
        response.setIsSynced(note.getIsSynced());
        return response;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "mp3";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "mp3";
    }
}
