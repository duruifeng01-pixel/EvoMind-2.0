package com.evomind.repository;

import com.evomind.entity.ContentCrawlJob;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContentCrawlJobRepository extends JpaRepository<ContentCrawlJob, Long> {

    List<ContentCrawlJob> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<ContentCrawlJob> findBySourceIdAndStatus(Long sourceId, ContentCrawlJob.JobStatus status);

    Optional<ContentCrawlJob> findFirstBySourceIdOrderByCreatedAtDesc(Long sourceId);

    @Query("SELECT job FROM ContentCrawlJob job WHERE job.status = :status AND job.retryCount < :maxRetries")
    List<ContentCrawlJob> findFailedJobsForRetry(
            @Param("status") ContentCrawlJob.JobStatus status,
            @Param("maxRetries") int maxRetries);

    @Query("SELECT job FROM ContentCrawlJob job " +
           "WHERE job.sourceId = :sourceId AND job.status = 'COMPLETED' " +
           "ORDER BY job.completedAt DESC")
    List<ContentCrawlJob> findCompletedJobsBySource(@Param("sourceId") Long sourceId, Pageable pageable);

    long countByUserIdAndStatus(Long userId, ContentCrawlJob.JobStatus status);

    @Query("SELECT SUM(job.articlesNew) FROM ContentCrawlJob job WHERE job.userId = :userId AND job.status = 'COMPLETED'")
    Long sumArticlesNewByUserId(@Param("userId") Long userId);
}
