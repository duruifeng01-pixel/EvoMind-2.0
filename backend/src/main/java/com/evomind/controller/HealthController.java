package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success("服务运行正常", Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0"
        ));
    }
}
