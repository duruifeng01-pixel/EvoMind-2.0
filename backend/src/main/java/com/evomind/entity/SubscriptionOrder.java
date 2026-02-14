package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_orders")
@Getter
@Setter
public class SubscriptionOrder extends BaseEntity {

    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Channel channel;

    @Column(name = "compute_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal computeCost;

    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.INIT;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "transaction_id", length = 128)
    private String transactionId;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_at")
    private LocalDateTime refundAt;

    public enum Channel {
        WECHAT, ALIPAY
    }

    public enum OrderStatus {
        INIT, PAID, FAILED, REFUNDED, CANCELLED
    }
}
