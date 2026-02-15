package com.evomind.repository;

import com.evomind.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByIdAndUserId(Long id, Long userId);

    List<Card> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Card> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Card> findByUserIdAndIsFavoriteTrueOrderByCreatedAtDesc(Long userId);

    List<Card> findByUserIdAndSourceIdOrderByCreatedAtDesc(Long userId, Long sourceId);

    @Query("SELECT c FROM Card c WHERE c.userId = :userId AND c.isArchived = false " +
           "AND (c.title LIKE %:keyword% OR c.summaryText LIKE %:keyword%)")
    List<Card> searchByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Modifying
    @Query("UPDATE Card c SET c.viewCount = c.viewCount + 1, c.lastViewedAt = :now WHERE c.id = :id")
    void incrementViewCount(@Param("id") Long id, @Param("now") LocalDateTime now);

    long countByUserId(Long userId);

    long countByUserIdAndIsFavoriteTrue(Long userId);

    // ===== Feed流相关查询 =====

    /**
     * 根据关键词查找卡片（用于推荐）
     */
    @Query("SELECT c FROM Card c WHERE c.keywords LIKE %:keyword% AND c.id NOT IN :excludeIds")
    List<Card> findByKeywordsContainingAndIdNotIn(
            @Param("keyword") String keyword,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    /**
     * 获取用户关注信息源的最新卡片（70%自选源内容）
     */
    @Query("SELECT c FROM Card c WHERE c.sourceId IN " +
           "(SELECT s.id FROM Source s WHERE s.userId = :userId AND s.isEnabled = true) " +
           "ORDER BY c.createdAt DESC")
    List<Card> findByUserSourcesOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable);

    /**
     * 排除指定ID后按创建时间排序
     */
    @Query("SELECT c FROM Card c WHERE c.id NOT IN :excludeIds ORDER BY c.createdAt DESC")
    List<Card> findByIdNotInOrderByCreatedAtDesc(
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    /**
     * 排除指定类别后随机获取卡片（多样性推荐）
     */
    @Query(value = "SELECT * FROM cards WHERE category NOT IN :categories ORDER BY RAND()",
           nativeQuery = true)
    List<Card> findByCategoryNotIn(
            @Param("categories") Set<String> categories,
            Pageable pageable);

    /**
     * 随机获取指定数量的卡片
     */
    @Query(value = "SELECT * FROM cards ORDER BY RAND() LIMIT :limit",
           nativeQuery = true)
    List<Card> findRandomCards(@Param("limit") int limit);

    /**
     * 排除指定ID后随机获取卡片
     */
    @Query(value = "SELECT * FROM cards WHERE id NOT IN :excludeIds ORDER BY RAND() LIMIT :limit",
           nativeQuery = true)
    List<Card> findRandomCardsExcluding(
            @Param("excludeIds") Set<Long> excludeIds,
            Pageable pageable);
}
