package com.evomind.repository;

import com.evomind.entity.VoiceNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 语音笔记数据访问层
 */
@Repository
public interface VoiceNoteRepository extends JpaRepository<VoiceNote, Long> {

    /**
     * 根据用户ID分页查询语音笔记
     */
    Page<VoiceNote> findByUserIdOrderByRecordedAtDesc(Long userId, Pageable pageable);

    /**
     * 查询用户所有未归档的语音笔记
     */
    List<VoiceNote> findByUserIdAndIsArchivedFalseOrderByRecordedAtDesc(Long userId);

    /**
     * 查询用户收藏的语音笔记
     */
    Page<VoiceNote> findByUserIdAndIsFavoriteTrueOrderByRecordedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据ID和用户ID查询语音笔记
     */
    Optional<VoiceNote> findByIdAndUserId(Long id, Long userId);

    /**
     * 查询用户待转写的语音笔记
     */
    List<VoiceNote> findByUserIdAndTranscribeStatusOrderByRecordedAtAsc(Long userId, String transcribeStatus);

    /**
     * 模糊搜索语音笔记内容
     */
    @Query("SELECT v FROM VoiceNote v WHERE v.userId = :userId AND " +
           "(LOWER(v.transcribedText) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "v.isArchived = false " +
           "ORDER BY v.recordedAt DESC")
    Page<VoiceNote> searchByKeyword(@Param("userId") Long userId, 
                                     @Param("keyword") String keyword, 
                                     Pageable pageable);

    /**
     * 查询用户在时间范围内的语音笔记数量
     */
    @Query("SELECT COUNT(v) FROM VoiceNote v WHERE v.userId = :userId AND " +
           "v.recordedAt BETWEEN :startTime AND :endTime AND v.isArchived = false")
    Long countByUserIdAndRecordedAtBetween(@Param("userId") Long userId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户总的录音时长（秒）
     */
    @Query("SELECT COALESCE(SUM(v.audioDurationSeconds), 0) FROM VoiceNote v " +
           "WHERE v.userId = :userId AND v.isArchived = false")
    Integer sumAudioDurationByUserId(@Param("userId") Long userId);

    /**
     * 查询用户未同步的语音笔记
     */
    List<VoiceNote> findByUserIdAndIsSyncedFalse(Long userId);
}
