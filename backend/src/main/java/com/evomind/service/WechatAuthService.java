package com.evomind.service;

import com.evomind.entity.User;

import java.util.Optional;

public interface WechatAuthService {

    /**
     * 微信登录
     * @param code 微信授权码
     * @return 登录成功的用户
     */
    User wechatLogin(String code);

    /**
     * 绑定微信到现有用户
     * @param userId 用户ID
     * @param code 微信授权码
     * @return 是否绑定成功
     */
    boolean bindWechat(Long userId, String code);

    /**
     * 解绑微信
     * @param userId 用户ID
     * @return 是否解绑成功
     */
    boolean unbindWechat(Long userId);

    /**
     * 根据openid查找用户
     * @param openid 微信openid
     * @return 用户Optional
     */
    Optional<User> findByOpenid(String openid);
}
