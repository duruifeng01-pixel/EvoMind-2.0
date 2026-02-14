package com.evomind.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ai_call_logs")
@Getter
@Setter
public class AiCallLog extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "scene_code", nullable = false, length = 64)
    private String sceneCode;

    @Column(name = "model_name", nullable = false, length = 128)
    private String modelName;

    @Column(name = "token_in")
    private Integer tokenIn = 0;

    @Column(name = "token_out")
    private Integer tokenOut = 0;

    @Column(name = "latency_ms")
    private Integer latencyMs = 0;

    @Column(name = "cost_amount", precision = 10, scale = 4)
    private BigDecimal costAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean success = true;

    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Column(name = "request_hash", length = 64)
    private String requestHash;
}
