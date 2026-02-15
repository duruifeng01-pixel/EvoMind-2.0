package com.evomind.repository;

import com.evomind.entity.UserInterestProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInterestProfileRepository extends JpaRepository<UserInterestProfile, Long> {

    List<UserInterestProfile> findByUserIdAndIsActiveTrue(Long userId);

    Optional<UserInterestProfile> findByUserIdAndInterestTag(Long userId, String interestTag);

    @Query("SELECT uip FROM UserInterestProfile uip WHERE uip.userId = :userId AND uip.isActive = true ORDER BY uip.weight DESC")
    List<UserInterestProfile> findTopInterestsByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT uip.category, SUM(uip.weight) as totalWeight " +
           "FROM UserInterestProfile uip " +
           "WHERE uip.userId = :userId AND uip.isActive = true " +
           "GROUP BY uip.category " +
           "ORDER BY totalWeight DESC")
    List<Object[]> findCategoryDistributionByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndInterestTag(Long userId, String interestTag);

    long countByUserIdAndIsActiveTrue(Long userId);
}
