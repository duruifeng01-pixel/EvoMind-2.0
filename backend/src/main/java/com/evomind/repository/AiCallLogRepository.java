package com.evomind.repository;

import com.evomind.entity.AiCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiCallLogRepository extends JpaRepository<AiCallLog, Long> {

    List<AiCallLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AiCallLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(l.tokenIn), 0) FROM AiCallLog l WHERE l.userId = :userId " +
           "AND l.createdAt BETWEEN :start AND :end")
    Integer sumTokenInByUserAndTimeRange(@Param("userId") Long userId, 
                                         @Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(l.tokenOut), 0) FROM AiCallLog l WHERE l.userId = :userId " +
           "AND l.createdAt BETWEEN :start AND :end")
    Integer sumTokenOutByUserAndTimeRange(@Param("userId") Long userId, 
                                          @Param("start") LocalDateTime start, 
                                          @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(l.costAmount), 0) FROM AiCallLog l WHERE l.userId = :userId " +
           "AND l.createdAt BETWEEN :start AND :end")
    java.math.BigDecimal sumCostByUserAndTimeRange(@Param("userId") Long userId, 
                                                    @Param("start") LocalDateTime start, 
                                                    @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(COUNT(l), 0) FROM AiCallLog l WHERE l.userId = :userId " +
           "AND l.success = false AND l.createdAt BETWEEN :start AND :end")
    Long countFailedCallsByUserAndTimeRange(@Param("userId") Long userId, 
                                            @Param("start") LocalDateTime start, 
                                            @Param("end") LocalDateTime end);

    @Query("SELECT l.sceneCode, COUNT(l) as count FROM AiCallLog l " +
           "WHERE l.userId = :userId AND l.createdAt BETWEEN :start AND :end " +
           "GROUP BY l.sceneCode ORDER BY count DESC")
    List<Object[]> countBySceneCode(@Param("userId") Long userId, 
                                    @Param("start") LocalDateTime start, 
                                    @Param("end") LocalDateTime end);
}
