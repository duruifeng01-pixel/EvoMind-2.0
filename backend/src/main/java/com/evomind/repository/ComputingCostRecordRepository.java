package com.evomind.repository;

import com.evomind.entity.ComputingCostRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComputingCostRecordRepository extends JpaRepository<ComputingCostRecord, Long> {

    Optional<ComputingCostRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    List<ComputingCostRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(c.totalCost) FROM ComputingCostRecord c WHERE c.userId = :userId AND c.recordDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalCostByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(c.subscriptionFee) FROM ComputingCostRecord c WHERE c.userId = :userId AND c.recordDate BETWEEN :startDate AND :endDate")
    BigDecimal sumSubscriptionFeeByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(c.aiTokenCount) FROM ComputingCostRecord c WHERE c.userId = :userId AND c.recordDate BETWEEN :startDate AND :endDate")
    Long sumAiTokenCountByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT " +
           "SUM(c.sourceCount), " +
           "SUM(c.conflictMarkCount), " +
           "SUM(c.ocrRequestCount), " +
           "SUM(c.aiTokenCount), " +
           "SUM(c.dialogueTurnCount), " +
           "SUM(c.modelTrainingCount), " +
           "SUM(c.feedCardCount), " +
           "SUM(c.crawlRequestCount) " +
           "FROM ComputingCostRecord c WHERE c.userId = :userId AND c.recordDate BETWEEN :startDate AND :endDate")
    Object[] sumMetricsByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
