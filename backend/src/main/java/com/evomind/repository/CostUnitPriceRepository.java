package com.evomind.repository;

import com.evomind.entity.CostUnitPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CostUnitPriceRepository extends JpaRepository<CostUnitPrice, Long> {

    Optional<CostUnitPrice> findByPriceCode(String priceCode);

    @Query("SELECT p FROM CostUnitPrice p WHERE p.priceCode = :priceCode " +
           "AND p.isActive = true " +
           "AND p.effectiveDate <= :currentTime " +
           "AND (p.expiryDate IS NULL OR p.expiryDate > :currentTime)")
    Optional<CostUnitPrice> findActiveByPriceCodeAndDate(
            @Param("priceCode") String priceCode,
            @Param("currentTime") LocalDateTime currentTime);

    List<CostUnitPrice> findByIsActiveTrueAndServiceCategoryOrderByPriceCodeAsc(String serviceCategory);

    List<CostUnitPrice> findByIsActiveTrueOrderByServiceCategoryAscPriceCodeAsc();
}
