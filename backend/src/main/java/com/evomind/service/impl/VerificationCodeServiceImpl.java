package com.evomind.service.impl;

import com.evomind.service.SmsService;
import com.evomind.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private final SmsService smsService;

    private static final String CODE_KEY_PREFIX = "verify:code:";
    private static final String COOLDOWN_KEY_PREFIX = "verify:cooldown:";
    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final long COOLDOWN_SECONDS = 60;

    @Override
    public boolean sendCode(String phone) {
        // 检查冷却时间
        if (getCooldownSeconds(phone) > 0) {
            log.warn("验证码发送过于频繁: {}", phone);
            return false;
        }

        // 生成6位验证码
        String code = generateCode();

        // 保存到Redis
        String codeKey = CODE_KEY_PREFIX + phone;
        String cooldownKey = COOLDOWN_KEY_PREFIX + phone;

        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);

        // 发送短信
        boolean success = smsService.sendVerificationCode(phone, code);

        if (success) {
            log.info("验证码已发送: {}, code: {}", phone, code);
        } else {
            log.error("验证码发送失败: {}", phone);
            // 清理Redis
            redisTemplate.delete(codeKey);
            redisTemplate.delete(cooldownKey);
        }

        return success;
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }

        String codeKey = CODE_KEY_PREFIX + phone;
        String cachedCode = redisTemplate.opsForValue().get(codeKey);

        if (cachedCode == null) {
            log.warn("验证码已过期或不存在: {}", phone);
            return false;
        }

        boolean valid = cachedCode.equals(code);

        if (valid) {
            // 验证通过后删除验证码
            redisTemplate.delete(codeKey);
            log.info("验证码验证成功: {}", phone);
        } else {
            log.warn("验证码错误: {}, expected: {}, actual: {}", phone, cachedCode, code);
        }

        return valid;
    }

    @Override
    public long getCooldownSeconds(String phone) {
        String cooldownKey = COOLDOWN_KEY_PREFIX + phone;
        Long expire = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
        return expire != null && expire > 0 ? expire : 0;
    }

    private String generateCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
