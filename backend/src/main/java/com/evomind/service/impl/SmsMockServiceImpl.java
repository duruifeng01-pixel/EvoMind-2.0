package com.evomind.service.impl;

import com.evomind.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "sms", name = "enabled", havingValue = "false", matchIfMissing = true)
public class SmsMockServiceImpl implements SmsService {

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        log.info("[模拟短信] 发送验证码到 {}: {}", phone, code);
        return true;
    }

    @Override
    public boolean sendSms(String phone, String templateCode, String params) {
        log.info("[模拟短信] 发送短信到 {}, 模板: {}, 参数: {}", phone, templateCode, params);
        return true;
    }
}
