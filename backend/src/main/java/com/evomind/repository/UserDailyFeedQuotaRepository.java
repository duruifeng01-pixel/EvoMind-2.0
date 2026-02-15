package com.evomind.repository;

import com.evomind.entity.UserDailyFeedQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 用户每日Feed配额Repository
 */
@Repository
public interface UserDailyFeedQuotaRepository extends JpaRepository<UserDailyFeedQuota, Long> {

    Optional<UserDailyFeedQuota> findByUserIdAndQuotaDate(Long userId, LocalDate quotaDate);

    boolean existsByUserIdAndQuotaDate(Long userId, LocalDate quotaDate);

    @Modifying
    @Query("UPDATE UserDailyFeedQuota q SET q.isExhausted = true, q.exhaustedAt = CURRENT_TIMESTAMP " +
           "WHERE q.userId = :userId AND q.quotaDate = :date")
    void markExhausted(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT SUM(q.consumedCount) FROM UserDailyFeedQuota q WHERE q.userId = :userId")
    Long sumTotalConsumedByUserId(@Param("userId") Long userId);
}
