package com.evomind.controller;

import com.evomind.dto.ApiResponse;
import com.evomind.dto.request.*;
import com.evomind.dto.response.AuthResponse;
import com.evomind.entity.User;
import com.evomind.security.JwtUtil;
import com.evomind.service.UserService;
import com.evomind.service.VerificationCodeService;
import com.evomind.service.WechatAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "认证", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final VerificationCodeService verificationCodeService;
    private final WechatAuthService wechatAuthService;

    @Operation(summary = "密码登录", description = "使用手机号和密码登录")
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

        log.info("用户密码登录成功: {}", request.getPhone());
        return ApiResponse.success("登录成功", response);
    }

    @Operation(summary = "验证码登录/注册", description = "使用手机号和验证码登录，未注册用户自动注册")
    @PostMapping("/login-by-code")
    public ApiResponse<AuthResponse> loginByCode(@Valid @RequestBody PhoneLoginRequest request) {
        // 验证验证码
        if (!verificationCodeService.verifyCode(request.getPhone(), request.getVerificationCode())) {
            return ApiResponse.error("验证码错误或已过期");
        }

        // 检查用户是否存在，不存在则自动注册
        User user;
        boolean isNewUser = false;
        if (!userService.existsByPhone(request.getPhone())) {
            user = userService.createPhoneUser(request.getPhone(), null);
            isNewUser = true;
            log.info("新用户自动注册: {}", request.getPhone());
        } else {
            user = userService.findByPhone(request.getPhone());
        }

        // 生成token
        String token = jwtUtil.generateToken(request.getPhone());

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(userInfo)
                .isNewUser(isNewUser)
                .build();

        log.info("用户验证码登录成功: {}, 新用户: {}", request.getPhone(), isNewUser);
        return ApiResponse.success(isNewUser ? "注册并登录成功" : "登录成功", response);
    }

    @Operation(summary = "用户注册", description = "使用手机号、密码和验证码注册")
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 验证验证码
        if (!verificationCodeService.verifyCode(request.getPhone(), request.getVerificationCode())) {
            return ApiResponse.error("验证码错误或已过期");
        }

        // 检查手机号是否已注册
        if (userService.existsByPhone(request.getPhone())) {
            return ApiResponse.error("该手机号已注册");
        }

        // 创建用户
        User user = userService.createPhoneUser(request.getPhone(), request.getPassword());

        // 更新昵称（如果提供）
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            userService.updateUserInfo(user.getId(), request.getNickname(), null);
        }

        // 生成token
        String token = jwtUtil.generateToken(request.getPhone());

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(request.getNickname() != null ? request.getNickname() : user.getNickname())
                .avatar(user.getAvatar())
                .build();

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(userInfo)
                .isNewUser(true)
                .build();

        log.info("用户注册成功: {}", request.getPhone());
        return ApiResponse.success("注册成功", response);
    }

    @Operation(summary = "发送验证码", description = "发送短信验证码到指定手机号")
    @PostMapping("/send-verification-code")
    public ApiResponse<Void> sendVerificationCode(@Valid @RequestBody SendCodeRequest request) {
        // 检查冷却时间
        long cooldown = verificationCodeService.getCooldownSeconds(request.getPhone());
        if (cooldown > 0) {
            return ApiResponse.error("请" + cooldown + "秒后再试");
        }

        boolean success = verificationCodeService.sendCode(request.getPhone());
        if (success) {
            log.info("验证码发送成功: {}", request.getPhone());
            return ApiResponse.success("验证码已发送", null);
        } else {
            log.error("验证码发送失败: {}", request.getPhone());
            return ApiResponse.error("验证码发送失败，请稍后重试");
        }
    }

    @Operation(summary = "微信登录", description = "使用微信授权码登录")
    @PostMapping("/wechat-login")
    public ApiResponse<AuthResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        User user = wechatAuthService.wechatLogin(request.getCode());

        // 生成token
        String phone = user.getPhone() != null ? user.getPhone() : "wechat_" + user.getWechatOpenid();
        String token = jwtUtil.generateToken(phone);

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(userInfo)
                .isNewUser(user.getPhone() == null) // 未绑定手机号视为新用户
                .build();

        log.info("微信登录成功: {}, openid: {}", user.getId(), user.getWechatOpenid());
        return ApiResponse.success("登录成功", response);
    }

    @Operation(summary = "刷新Token", description = "刷新JWT令牌")
    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error("无效的Token格式");
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            return ApiResponse.error("Token无效或已过期");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        String newToken = jwtUtil.generateToken(username);

        AuthResponse response = AuthResponse.builder()
                .token(newToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .build();

        return ApiResponse.success("Token刷新成功", response);
    }

    @Operation(summary = "检查手机号是否注册", description = "检查手机号是否已注册")
    @GetMapping("/check-phone")
    public ApiResponse<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = userService.existsByPhone(phone);
        return ApiResponse.success(exists ? "手机号已注册" : "手机号未注册", exists);
    }
}
