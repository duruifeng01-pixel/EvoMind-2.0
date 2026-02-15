package com.evomind.service.impl;

import com.evomind.dto.response.AuthResponse;
import com.evomind.entity.User;
import com.evomind.exception.BusinessException;
import com.evomind.repository.UserRepository;
import com.evomind.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse.UserInfo getCurrentUserInfo(String phone) {
        User user = findByPhone(phone);
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    @Transactional
    public User createPhoneUser(String phone, String password) {
        if (existsByPhone(phone)) {
            throw new BusinessException("该手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        } else {
            // 生成随机密码（验证码登录用户）
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        }
        user.setNickname(generateNickname());
        user.setEnabled(true);
        user.setLastLoginAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User createWechatUser(String openid, String nickname, String avatar) {
        User user = new User();
        user.setWechatOpenid(openid);
        user.setNickname(nickname != null ? nickname : generateNickname());
        user.setAvatar(avatar);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setEnabled(true);
        user.setLastLoginAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUserInfo(Long userId, String nickname, String avatar) {
        User user = findById(userId);
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User bindPhone(Long userId, String phone) {
        if (existsByPhone(phone)) {
            throw new BusinessException("该手机号已被其他账号绑定");
        }

        User user = findById(userId);
        user.setPhone(phone);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Override
    @Transactional
    public User resetPassword(String phone, String newPassword) {
        User user = findByPhone(phone);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    private String generateNickname() {
        return "用户" + System.currentTimeMillis() % 1000000;
    }
}
