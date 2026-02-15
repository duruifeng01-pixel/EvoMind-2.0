package com.evomind.repository;

import com.evomind.entity.UserReadingHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserReadingHistoryRepository extends JpaRepository<UserReadingHistory, Long> {

    List<UserReadingHistory> findByUserIdOrderByReadAtDesc(Long userId, Pageable pageable);

    Optional<UserReadingHistory> findByUserIdAndCardId(Long userId, Long cardId);

    @Query("SELECT urh.sourceId, COUNT(urh) as readCount, SUM(urh.readDurationSeconds) as totalDuration " +
           "FROM UserReadingHistory urh " +
           "WHERE urh.userId = :userId AND urh.readAt >= :since " +
           "GROUP BY urh.sourceId " +
           "ORDER BY readCount DESC")
    List<Object[]> findSourceReadingStats(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT urh.keywords FROM UserReadingHistory urh " +
           "WHERE urh.userId = :userId AND urh.readAt >= :since AND urh.keywords IS NOT NULL")
    List<String> findRecentKeywords(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT urh.cardId FROM UserReadingHistory urh WHERE urh.userId = :userId")
    List<Long> findReadCardIdsByUserId(@Param("userId") Long userId);

    long countByUserIdAndIsFavoriteTrue(Long userId);

    @Query("SELECT AVG(urh.readPercentage) FROM UserReadingHistory urh WHERE urh.userId = :userId")
    Double calculateAverageReadPercentage(@Param("userId") Long userId);
}
