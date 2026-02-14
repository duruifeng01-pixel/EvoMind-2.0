package com.evomind.repository;

import com.evomind.entity.SourceImportJob;
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
 * 信息源导入任务Repository
 */
@Repository
public interface SourceImportJobRepository extends JpaRepository<SourceImportJob, Long> {

    /**
     * 查询用户的导入任务列表
     */
    Page<SourceImportJob> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 查询指定状态的任务
     */
    List<SourceImportJob> findByStatus(SourceImportJob.JobStatus status);

    /**
     * 查询用户待处理或处理中的任务
     */
    @Query("SELECT j FROM SourceImportJob j WHERE j.userId = :userId AND j.status IN ('PENDING', 'PROCESSING', 'RETRYING')")
    List<SourceImportJob> findActiveJobsByUserId(@Param("userId") Long userId);

    /**
     * 查询指定任务（验证归属）
     */
    Optional<SourceImportJob> findByIdAndUserId(Long id, Long userId);

    /**
     * 查询需要重试的失败任务
     */
    @Query("SELECT j FROM SourceImportJob j WHERE j.status = 'FAILED' AND j.retryCount < :maxRetries AND j.createdAt > :since")
    List<SourceImportJob> findRetryableJobs(@Param("maxRetries") Integer maxRetries, @Param("since") LocalDateTime since);

    /**
     * 统计用户今日导入次数
     */
    @Query("SELECT COUNT(j) FROM SourceImportJob j WHERE j.userId = :userId AND DATE(j.createdAt) = CURRENT_DATE")
    Long countTodayImportsByUserId(@Param("userId") Long userId);
}
