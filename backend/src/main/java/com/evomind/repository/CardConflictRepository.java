package com.evomind.repository;

import com.evomind.entity.CardConflict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardConflictRepository extends JpaRepository<CardConflict, Long> {

    List<CardConflict> findByUserIdAndIsAcknowledgedFalse(Long userId);

    List<CardConflict> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT c FROM CardConflict c WHERE (c.cardId1 = :cardId OR c.cardId2 = :cardId) AND c.userId = :userId")
    List<CardConflict> findByCardIdAndUserId(@Param("cardId") Long cardId, @Param("userId") Long userId);

    @Query("SELECT c FROM CardConflict c WHERE ((c.cardId1 = :cardId1 AND c.cardId2 = :cardId2) OR (c.cardId1 = :cardId2 AND c.cardId2 = :cardId1)) AND c.userId = :userId")
    Optional<CardConflict> findConflictBetweenCards(@Param("cardId1") Long cardId1, @Param("cardId2") Long cardId2, @Param("userId") Long userId);

    long countByUserIdAndIsAcknowledgedFalse(Long userId);

    long countByUserId(Long userId);

    boolean existsByCardId1AndCardId2AndUserId(Long cardId1, Long cardId2, Long userId);
}
