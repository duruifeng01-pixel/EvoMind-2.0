package com.evomind.service.impl;

import com.evomind.config.WechatConfig;
import com.evomind.entity.User;
import com.evomind.exception.BusinessException;
import com.evomind.repository.UserRepository;
import com.evomind.service.UserService;
import com.evomind.service.WechatAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatAuthServiceImpl implements WechatAuthService {

    private final WechatConfig wechatConfig;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private static final String WX_USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo";

    @Override
    @Transactional
    public User wechatLogin(String code) {
        // 1. 通过code获取access_token和openid
        String tokenUrl = String.format("%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                WX_LOGIN_URL, wechatConfig.getAppId(), wechatConfig.getAppSecret(), code);

        ResponseEntity<Map> tokenResponse = restTemplate.getForEntity(tokenUrl, Map.class);
        Map<String, Object> tokenData = tokenResponse.getBody();

        if (tokenData == null || tokenData.containsKey("errcode")) {
            log.error("微信登录失败: {}", tokenData);
            throw new BusinessException("微信授权失败");
        }

        String openid = (String) tokenData.get("openid");
        String accessToken = (String) tokenData.get("access_token");

        // 2. 查询是否已存在该用户
        Optional<User> existingUser = userRepository.findByWechatOpenid(openid);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setLastLoginAt(LocalDateTime.now());
            return userRepository.save(user);
        }

        // 3. 获取用户信息（新用户）
        String userInfoUrl = String.format("%s?access_token=%s&openid=%s",
                WX_USERINFO_URL, accessToken, openid);

        ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
        Map<String, Object> userInfo = userInfoResponse.getBody();

        if (userInfo == null || userInfo.containsKey("errcode")) {
            log.error("获取微信用户信息失败: {}", userInfo);
            throw new BusinessException("获取微信用户信息失败");
        }

        // 4. 创建新用户
        String nickname = (String) userInfo.get("nickname");
        String headimgurl = (String) userInfo.get("headimgurl");

        return userService.createWechatUser(openid, nickname, headimgurl);
    }

    @Override
    @Transactional
    public boolean bindWechat(Long userId, String code) {
        // 获取openid
        String tokenUrl = String.format("%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                WX_LOGIN_URL, wechatConfig.getAppId(), wechatConfig.getAppSecret(), code);

        ResponseEntity<Map> tokenResponse = restTemplate.getForEntity(tokenUrl, Map.class);
        Map<String, Object> tokenData = tokenResponse.getBody();

        if (tokenData == null || tokenData.containsKey("errcode")) {
            log.error("微信绑定失败: {}", tokenData);
            return false;
        }

        String openid = (String) tokenData.get("openid");

        // 检查openid是否已被其他用户绑定
        Optional<User> existingUser = userRepository.findByWechatOpenid(openid);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            throw new BusinessException("该微信账号已被其他用户绑定");
        }

        // 绑定到当前用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setWechatOpenid(openid);
        userRepository.save(user);

        return true;
    }

    @Override
    @Transactional
    public boolean unbindWechat(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查是否还有其他登录方式
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            throw new BusinessException("请绑定手机号后再解绑微信");
        }

        user.setWechatOpenid(null);
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByOpenid(String openid) {
        return userRepository.findByWechatOpenid(openid);
    }
}
