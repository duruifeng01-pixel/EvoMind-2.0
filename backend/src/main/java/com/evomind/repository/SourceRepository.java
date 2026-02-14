package com.evomind.repository;

import com.evomind.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    Optional<Source> findByIdAndUserId(Long id, Long userId);

    List<Source> findByUserIdOrderByIsPinnedDescCreatedAtDesc(Long userId);

    List<Source> findByUserIdAndCategoryOrderByIsPinnedDescCreatedAtDesc(Long userId, String category);

    List<Source> findByUserIdAndEnabledTrueOrderByIsPinnedDescCreatedAtDesc(Long userId);

    Optional<Source> findByUserIdAndHomeUrl(Long userId, String homeUrl);

    boolean existsByUserIdAndHomeUrl(Long userId, String homeUrl);

    @Modifying
    @Query("UPDATE Source s SET s.articleCount = :count, s.lastSyncAt = :now WHERE s.id = :id")
    void updateArticleCount(@Param("id") Long id, @Param("count") Integer count, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Source s SET s.syncStatus = :status WHERE s.id = :id")
    void updateSyncStatus(@Param("id") Long id, @Param("status") String status);

    long countByUserId(Long userId);

    long countByUserIdAndEnabledTrue(Long userId);
}
