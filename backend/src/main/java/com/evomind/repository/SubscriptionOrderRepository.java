package com.evomind.repository;

import com.evomind.entity.SubscriptionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionOrderRepository extends JpaRepository<SubscriptionOrder, Long> {

    Optional<SubscriptionOrder> findByOrderNo(String orderNo);

    List<SubscriptionOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SubscriptionOrder> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, SubscriptionOrder.OrderStatus status);

    Optional<SubscriptionOrder> findByTransactionId(String transactionId);

    @Modifying
    @Query("UPDATE SubscriptionOrder o SET o.status = :status, o.paidAt = :paidAt, " +
           "o.transactionId = :transactionId WHERE o.orderNo = :orderNo")
    void updatePaidStatus(@Param("orderNo") String orderNo, 
                          @Param("status") SubscriptionOrder.OrderStatus status,
                          @Param("paidAt") LocalDateTime paidAt,
                          @Param("transactionId") String transactionId);

    @Modifying
    @Query("UPDATE SubscriptionOrder o SET o.status = 'REFUNDED', o.refundAmount = :amount, " +
           "o.refundAt = :now WHERE o.id = :id")
    void updateRefundStatus(@Param("id") Long id, 
                            @Param("amount") java.math.BigDecimal amount, 
                            @Param("now") LocalDateTime now);

    long countByUserIdAndStatus(Long userId, SubscriptionOrder.OrderStatus status);
}
