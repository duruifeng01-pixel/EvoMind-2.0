package com.evomind.repository;

import com.evomind.entity.UserReadCardRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户已读内容防重复记录Repository
 */
@Repository
public interface UserReadCardRecordRepository extends JpaRepository<UserReadCardRecord, Long> {

    Optional<UserReadCardRecord> findByUserIdAndCardId(Long userId, Long cardId);

    boolean existsByUserIdAndCardId(Long userId, Long cardId);

    @Query("SELECT r.cardId FROM UserReadCardRecord r WHERE r.userId = :userId AND r.coolDownUntil > :now")
    List<Long> findCardIdsInCoolDownPeriod(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserReadCardRecord r SET r.lastReadAt = :now, r.readCount = r.readCount + 1, " +
           "r.coolDownUntil = :coolDownUntil WHERE r.userId = :userId AND r.cardId = :cardId")
    void updateReadRecord(@Param("userId") Long userId, @Param("cardId") Long cardId,
                         @Param("now") LocalDateTime now, @Param("coolDownUntil") LocalDateTime coolDownUntil);

    @Modifying
    @Query("DELETE FROM UserReadCardRecord r WHERE r.coolDownUntil < :now")
    void deleteExpiredRecords(@Param("now") LocalDateTime now);

    long countByUserId(Long userId);
}
