package com.evomind.service.impl;

import com.evomind.entity.PlanCatalog;
import com.evomind.entity.SubscriptionOrder;
import com.evomind.entity.UserSubscription;
import com.evomind.exception.BusinessException;
import com.evomind.repository.PlanCatalogRepository;
import com.evomind.repository.SubscriptionOrderRepository;
import com.evomind.repository.UserSubscriptionRepository;
import com.evomind.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final PlanCatalogRepository planCatalogRepository;
    private final SubscriptionOrderRepository orderRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlanCatalog> getAvailablePlans() {
        return planCatalogRepository.findByEnabledTrueOrderBySortOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanCatalog getPlanByCode(String code) {
        return planCatalogRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("套餐不存在"));
    }

    @Override
    @Transactional
    public SubscriptionOrder createOrder(Long userId, String planCode, String channel) {
        PlanCatalog plan = getPlanByCode(planCode);
        
        // 计算算力成本
        BigDecimal computeCost = calculateComputeCost(userId);
        BigDecimal finalAmount = plan.getPrice().add(computeCost);
        
        SubscriptionOrder order = new SubscriptionOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPlanId(plan.getId());
        order.setChannel(SubscriptionOrder.Channel.valueOf(channel.toUpperCase()));
        order.setComputeCost(computeCost);
        order.setFinalAmount(finalAmount);
        order.setStatus(SubscriptionOrder.OrderStatus.INIT);
        
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionOrder getOrderByNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException("订单不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionOrder> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void handlePaymentCallback(String orderNo, String transactionId, boolean success) {
        SubscriptionOrder order = getOrderByNo(orderNo);
        
        if (success) {
            LocalDateTime now = LocalDateTime.now();
            orderRepository.updatePaidStatus(orderNo, SubscriptionOrder.OrderStatus.PAID, now, transactionId);
            
            // 创建或更新用户订阅
            activateSubscription(order);
        } else {
            order.setStatus(SubscriptionOrder.OrderStatus.FAILED);
            orderRepository.save(order);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserSubscription getUserSubscription(Long userId) {
        return subscriptionRepository.findByUserId(userId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasValidSubscription(Long userId) {
        return subscriptionRepository.findValidSubscription(userId, LocalDateTime.now()).isPresent();
    }

    @Override
    @Transactional
    public void cancelSubscription(Long userId, String reason) {
        UserSubscription subscription = subscriptionRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new BusinessException("没有有效的订阅"));
        
        subscriptionRepository.cancelSubscription(subscription.getId(), LocalDateTime.now(), reason);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateComputeCost(Long userId) {
        // TODO: 实现基于实际算力使用的成本计算
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public void processRefund(Long orderId, BigDecimal amount) {
        SubscriptionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        
        if (order.getStatus() != SubscriptionOrder.OrderStatus.PAID) {
            throw new BusinessException("订单未支付，无法退款");
        }
        
        orderRepository.updateRefundStatus(orderId, amount, LocalDateTime.now());
    }

    private void activateSubscription(SubscriptionOrder order) {
        PlanCatalog plan = planCatalogRepository.findById(order.getPlanId())
                .orElseThrow(() -> new BusinessException("套餐不存在"));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt;
        
        switch (plan.getPeriod()) {
            case WEEK:
                expireAt = now.plusWeeks(1);
                break;
            case MONTH:
                expireAt = now.plusMonths(1);
                break;
            case YEAR:
                expireAt = now.plusYears(1);
                break;
            default:
                expireAt = now.plusMonths(1);
        }
        
        // 停用现有订阅
        subscriptionRepository.findByUserIdAndActiveTrue(order.getUserId())
                .ifPresent(existing -> {
                    existing.setActive(false);
                    subscriptionRepository.save(existing);
                });
        
        // 创建新订阅
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(order.getUserId());
        subscription.setPlanId(order.getPlanId());
        subscription.setOrderId(order.getId());
        subscription.setStartAt(now);
        subscription.setExpireAt(expireAt);
        subscription.setActive(true);
        subscription.setAutoRenew(false);
        
        subscriptionRepository.save(subscription);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "EM" + timestamp + random;
    }
}
