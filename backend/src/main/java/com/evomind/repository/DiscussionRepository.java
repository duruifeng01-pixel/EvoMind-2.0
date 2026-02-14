package com.evomind.repository;

import com.evomind.entity.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 讨论主题Repository
 */
@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    /**
     * 根据日期Key查询
     */
    Optional<Discussion> findByDateKey(String dateKey);

    /**
     * 查询今日讨论
     */
    default Optional<Discussion> findToday() {
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return findByDateKey(today);
    }

    /**
     * 查询已发布的讨论列表
     */
    Page<Discussion> findByIsPublishedTrueOrderByDateKeyDesc(Pageable pageable);

    /**
     * 查询最近N天的讨论
     */
    @Query("SELECT d FROM Discussion d WHERE d.isPublished = true AND d.dateKey >= :startDate ORDER BY d.dateKey DESC")
    List<Discussion> findRecentDiscussions(@Param("startDate") String startDate);

    /**
     * 按标签查询讨论
     */
    Page<Discussion> findByTopicTagAndIsPublishedTrueOrderByDateKeyDesc(String topicTag, Pageable pageable);

    /**
     * 搜索讨论
     */
    @Query("SELECT d FROM Discussion d WHERE d.isPublished = true AND (d.title LIKE %:keyword% OR d.content LIKE %:keyword%) ORDER BY d.dateKey DESC")
    Page<Discussion> searchDiscussions(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 检查指定日期是否已有讨论
     */
    boolean existsByDateKey(String dateKey);
}
