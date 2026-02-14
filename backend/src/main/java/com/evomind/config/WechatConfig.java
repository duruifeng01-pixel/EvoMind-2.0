package com.evomind.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 微信配置
 */
@Data
@Configuration
public class WechatConfig {

    @Value("${wechat.mp.app-id:}")
    private String appId;

    @Value("${wechat.mp.app-secret:}")
    private String appSecret;

    @Value("${wechat.mp.token:}")
    private String token;

    @Value("${wechat.mp.aes-key:}")
    private String aesKey;
}
