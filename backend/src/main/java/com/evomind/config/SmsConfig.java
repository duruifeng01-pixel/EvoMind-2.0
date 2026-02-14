package com.evomind.config;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云短信服务配置
 */
@Data
@Configuration
public class SmsConfig {

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.endpoint:dysmsapi.aliyuncs.com}")
    private String endpoint;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.template-code.register:}")
    private String registerTemplateCode;

    @Value("${aliyun.sms.template-code.login:}")
    private String loginTemplateCode;

    /**
     * 创建阿里云SMS客户端
     */
    @Bean
    public Client smsClient() throws Exception {
        Config config = new Config()
            .setAccessKeyId(accessKeyId)
            .setAccessKeySecret(accessKeySecret)
            .setEndpoint(endpoint);
        return new Client(config);
    }
}
