package com.evomind.service;

import com.evomind.dto.request.RegisterRequest;
import com.evomind.dto.response.AuthResponse;
import com.evomind.entity.User;

public interface UserService {

    User findByPhone(String phone);

    boolean existsByPhone(String phone);

    AuthResponse.UserInfo getCurrentUserInfo(String phone);
}
