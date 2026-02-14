package com.evomind.repository;

import com.evomind.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    Optional<UserSubscription> findByUserId(Long userId);

    Optional<UserSubscription> findByUserIdAndActiveTrue(Long userId);

    @Query("SELECT s FROM UserSubscription s WHERE s.userId = :userId AND s.active = true " +
           "AND s.expireAt > :now")
    Optional<UserSubscription> findValidSubscription(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    boolean existsByUserIdAndActiveTrue(Long userId);

    @Modifying
    @Query("UPDATE UserSubscription s SET s.active = false, s.cancelledAt = :now, " +
           "s.cancelReason = :reason WHERE s.id = :id")
    void cancelSubscription(@Param("id") Long id, 
                           @Param("now") LocalDateTime now, 
                           @Param("reason") String reason);

    @Modifying
    @Query("UPDATE UserSubscription s SET s.active = false WHERE s.expireAt <= :now AND s.active = true")
    int deactivateExpiredSubscriptions(@Param("now") LocalDateTime now);
}
