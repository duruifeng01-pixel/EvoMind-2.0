package com.evomind.service;

public interface SmsService {

    /**
     * 发送短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String phone, String code);

    /**
     * 发送短信（通用）
     * @param phone 手机号
     * @param templateCode 模板代码
     * @param params 模板参数
     * @return 是否发送成功
     */
    boolean sendSms(String phone, String templateCode, String params);
}
