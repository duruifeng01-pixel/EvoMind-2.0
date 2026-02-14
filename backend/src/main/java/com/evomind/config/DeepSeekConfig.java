package com.evomind.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek API 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {
    
    /**
     * API 密钥
     */
    private String apiKey;
    
    /**
     * API 基础URL
     */
    private String baseUrl = "https://api.deepseek.com";
    
    /**
     * 模型名称
     */
    private String model = "deepseek-chat";
    
    /**
     * 请求超时时间（秒）
     */
    private int timeout = 60;
    
    /**
     * 最大重试次数
     */
    private int maxRetries = 3;
    
    /**
     * 温度参数（创造性）
     */
    private double temperature = 0.7;
    
    /**
     * 最大Token数
     */
    private int maxTokens = 4000;
}
