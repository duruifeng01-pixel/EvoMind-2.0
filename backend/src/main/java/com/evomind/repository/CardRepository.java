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
}
