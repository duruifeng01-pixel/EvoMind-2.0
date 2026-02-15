package com.evomind.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成本单价配置响应DTO
 */
@Data
@Builder
public class CostUnitPriceResponse {

    private Long id;
    private String priceCode;
    private String priceName;
    private String priceDescription;
    private BigDecimal unitPrice;
    private String unitType;
    private String serviceCategory;
    private Boolean isActive;
    private LocalDateTime effectiveDate;
    private LocalDateTime expiryDate;
}
