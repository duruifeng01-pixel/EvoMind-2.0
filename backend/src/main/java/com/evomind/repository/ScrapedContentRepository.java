package com.evomind.repository;

import com.evomind.entity.ScrapedContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 抓取内容数据访问层
 */
@Repository
public interface ScrapedContentRepository extends JpaRepository<ScrapedContent, Long> {

    /**
     * 根据任务ID查找
     */
    Optional<ScrapedContent> findByTaskId(String taskId);

    /**
     * 根据用户ID和任务ID查找
     */
    Optional<ScrapedContent> findByUserIdAndTaskId(Long userId, String taskId);

    /**
     * 根据URL查找（去重）
     */
    Optional<ScrapedContent> findBySourceUrl(String sourceUrl);

    /**
     * 根据用户ID和URL查找
     */
    Optional<ScrapedContent> findByUserIdAndSourceUrl(Long userId, String sourceUrl);

    /**
     * 根据内容Hash查找（去重）
     */
    Optional<ScrapedContent> findByContentHash(String contentHash);

    /**
     * 查询用户的抓取内容列表
     */
    Page<ScrapedContent> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 查询待处理的任务
     */
    java.util.List<ScrapedContent> findByStatusOrderByCreatedAtAsc(ScrapedContent.ScrapeStatus status);

    /**
     * 统计用户今日抓取次数
     */
    long countByUserIdAndCreatedAtAfter(Long userId, java.time.LocalDateTime startOfDay);
}
