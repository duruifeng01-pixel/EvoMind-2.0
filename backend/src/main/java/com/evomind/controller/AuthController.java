package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import com.evomind.dto.request.LoginRequest;
import com.evomind.dto.request.RegisterRequest;
import com.evomind.dto.response.AuthResponse;
import com.evomind.security.JwtUtil;
import com.evomind.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Operation(summary = "用户登录", description = "使用手机号和密码登录")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getPhone(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtil.generateToken(authentication);

        AuthResponse.UserInfo userInfo = userService.getCurrentUserInfo(request.getPhone());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(userInfo)
                .build();

        return ApiResponse.success("登录成功", response);
    }

    @Operation(summary = "用户注册", description = "使用手机号注册新账户")
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // TODO: 实现注册逻辑 (feat_003)
        return ApiResponse.error("注册功能待实现");
    }

    @Operation(summary = "发送验证码", description = "发送短信验证码")
    @PostMapping("/send-verification-code")
    public ApiResponse<Void> sendVerificationCode(@RequestParam String phone) {
        // TODO: 实现发送验证码逻辑 (feat_003)
        return ApiResponse.error("发送验证码功能待实现");
    }

    @Operation(summary = "微信登录", description = "使用微信授权登录")
    @PostMapping("/wechat-login")
    public ApiResponse<AuthResponse> wechatLogin(@RequestParam String code) {
        // TODO: 实现微信登录逻辑 (feat_003)
        return ApiResponse.error("微信登录功能待实现");
    }

    @Operation(summary = "刷新Token", description = "刷新JWT令牌")
    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(@RequestHeader("Authorization") String token) {
        // TODO: 实现刷新Token逻辑
        return ApiResponse.error("刷新Token功能待实现");
    }
}
