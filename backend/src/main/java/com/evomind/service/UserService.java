package com.evomind.service;

import com.evomind.dto.request.RegisterRequest;
import com.evomind.dto.response.AuthResponse;
import com.evomind.entity.User;

public interface UserService {

    User findByPhone(String phone);

    boolean existsByPhone(String phone);

    AuthResponse.UserInfo getCurrentUserInfo(String phone);

    /**
     * 创建手机号用户
     * @param phone 手机号
     * @param password 密码（可选）
     * @return 创建的用户
     */
    User createPhoneUser(String phone, String password);

    /**
     * 创建微信用户
     * @param openid 微信openid
     * @param nickname 昵称
     * @param avatar 头像URL
     * @return 创建的用户
     */
    User createWechatUser(String openid, String nickname, String avatar);

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param nickname 昵称
     * @param avatar 头像URL
     * @return 更新后的用户
     */
    User updateUserInfo(Long userId, String nickname, String avatar);

    /**
     * 绑定手机号
     * @param userId 用户ID
     * @param phone 手机号
     * @return 更新后的用户
     */
    User bindPhone(Long userId, String phone);

    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户
     */
    User findById(Long id);
}
