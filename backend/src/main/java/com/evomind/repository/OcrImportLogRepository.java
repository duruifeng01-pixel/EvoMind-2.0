package com.evomind.repository;

import com.evomind.entity.OcrImportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * OCR导入日志数据访问层
 */
@Repository
public interface OcrImportLogRepository extends JpaRepository<OcrImportLog, Long> {

    /**
     * 根据任务ID查找日志
     */
    Optional<OcrImportLog> findByTaskId(String taskId);

    /**
     * 根据用户ID和任务ID查找
     */
    Optional<OcrImportLog> findByUserIdAndTaskId(Long userId, String taskId);

    /**
     * 根据图片Hash查找(用于去重)
     */
    Optional<OcrImportLog> findByUserIdAndImageHash(Long userId, String imageHash);

    /**
     * 统计用户今日识别次数
     */
    long countByUserIdAndCreatedAtAfter(Long userId, java.time.LocalDateTime startOfDay);
}
