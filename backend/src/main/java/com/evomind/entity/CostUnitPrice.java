package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成本单价配置实体
 * 存储各项AI服务的成本单价配置
 */
@Data
@Entity
@Table(name = "cost_unit_prices")
@EqualsAndHashCode(callSuper = true)
public class CostUnitPrice extends BaseEntity {

    @Column(name = "price_code", nullable = false, unique = true, length = 50)
    private String priceCode;

    @Column(name = "price_name", nullable = false, length = 100)
    private String priceName;

    @Column(name = "price_description", length = 500)
    private String priceDescription;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 6)
    private BigDecimal unitPrice;

    @Column(name = "unit_type", nullable = false, length = 50)
    private String unitType; // 单位：per_request, per_token, per_mb, per_day

    @Column(name = "service_category", nullable = false, length = 50)
    private String serviceCategory; // ocr, ai, crawl, storage

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    // 预设价格代码常量
    public static final String PRICE_OCR_PER_REQUEST = "OCR_PER_REQUEST";
    public static final String PRICE_AI_PER_1K_TOKEN = "AI_PER_1K_TOKEN";
    public static final String PRICE_AI_DIALOGUE_PER_TURN = "AI_DIALOGUE_PER_TURN";
    public static final String PRICE_CRAWL_PER_REQUEST = "CRAWL_PER_REQUEST";
    public static final String PRICE_STORAGE_PER_MB_PER_DAY = "STORAGE_PER_MB_PER_DAY";
    public static final String PRICE_SOURCE_BASE_PER_DAY = "SOURCE_BASE_PER_DAY";
    public static final String PRICE_CONFLICT_MARK_PER_ITEM = "CONFLICT_MARK_PER_ITEM";
    public static final String PRICE_TRAINING_PER_SESSION = "TRAINING_PER_SESSION";
}
