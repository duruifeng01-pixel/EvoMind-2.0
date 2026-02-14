package com.evomind.repository;

import com.evomind.entity.PlanCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanCatalogRepository extends JpaRepository<PlanCatalog, Long> {

    Optional<PlanCatalog> findByCode(String code);

    List<PlanCatalog> findByEnabledTrueOrderBySortOrderAsc();

    boolean existsByCode(String code);
}
