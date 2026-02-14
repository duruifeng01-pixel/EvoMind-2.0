package com.evomind.service;

import com.evomind.entity.PlanCatalog;
import com.evomind.entity.SubscriptionOrder;
import com.evomind.entity.UserSubscription;

import java.math.BigDecimal;
import java.util.List;

public interface SubscriptionService {

    List<PlanCatalog> getAvailablePlans();

    PlanCatalog getPlanByCode(String code);

    SubscriptionOrder createOrder(Long userId, String planCode, String channel);

    SubscriptionOrder getOrderByNo(String orderNo);

    List<SubscriptionOrder> getOrdersByUserId(Long userId);

    void handlePaymentCallback(String orderNo, String transactionId, boolean success);

    UserSubscription getUserSubscription(Long userId);

    boolean hasValidSubscription(Long userId);

    void cancelSubscription(Long userId, String reason);

    BigDecimal calculateComputeCost(Long userId);

    void processRefund(Long orderId, BigDecimal amount);
}
