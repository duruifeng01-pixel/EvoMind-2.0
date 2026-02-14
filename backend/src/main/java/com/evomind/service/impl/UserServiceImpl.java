package com.evomind.service.impl;

import com.evomind.dto.response.AuthResponse;
import com.evomind.entity.User;
import com.evomind.exception.BusinessException;
import com.evomind.repository.UserRepository;
import com.evomind.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public AuthResponse.UserInfo getCurrentUserInfo(String phone) {
        User user = findByPhone(phone);
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }
}
