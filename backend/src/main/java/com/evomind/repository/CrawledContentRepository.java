package com.evomind.repository;

import com.evomind.entity.CrawledContent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CrawledContentRepository extends JpaRepository<CrawledContent, Long> {

    List<CrawledContent> findByUserIdAndStatusOrderByCrawledAtDesc(Long userId, CrawledContent.ContentStatus status);

    List<CrawledContent> findBySourceIdAndStatusOrderByCrawledAtDesc(Long sourceId, CrawledContent.ContentStatus status);

    Optional<CrawledContent> findByContentHash(String contentHash);

    boolean existsByContentHash(String contentHash);

    @Query("SELECT cc FROM CrawledContent cc WHERE cc.userId = :userId AND cc.status = 'COMPLETED' AND cc.isSystemDiscovered = false")
    List<CrawledContent> findUserSourceContent(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT cc FROM CrawledContent cc WHERE cc.status = 'COMPLETED' AND cc.isSystemDiscovered = true " +
           "AND cc.userId NOT IN (SELECT DISTINCT c.userId FROM CrawledContent c WHERE c.originalUrl = cc.originalUrl)")
    List<CrawledContent> findSystemDiscoveredContent(Pageable pageable);

    @Query("SELECT cc FROM CrawledContent cc WHERE cc.status = :status AND cc.crawledAt < :before")
    List<CrawledContent> findByStatusAndCrawledAtBefore(
            @Param("status") CrawledContent.ContentStatus status,
            @Param("before") LocalDateTime before);

    long countByUserIdAndStatus(Long userId, CrawledContent.ContentStatus status);

    long countBySourceIdAndIsDuplicate(Long sourceId, Boolean isDuplicate);
}
