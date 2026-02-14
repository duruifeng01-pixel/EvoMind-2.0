package com.evomind.service;

public interface VerificationCodeService {

    /**
     * 生成并发送验证码
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendCode(String phone);

    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String phone, String code);

    /**
     * 获取剩余冷却时间（秒）
     * @param phone 手机号
     * @return 剩余秒数，0表示可以发送
     */
    long getCooldownSeconds(String phone);
}
