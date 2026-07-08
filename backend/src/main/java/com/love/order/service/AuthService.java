package com.love.order.service;

import cn.dev33.satoken.stp.StpUtil;
import com.love.order.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    /** 账号密码登录 */
    public Map<String, Object> login(String username, String password) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("请输入用户名");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("请输入密码");

        User user = userService.getByUsername(username.trim());
        if (user == null || user.getPasswordHash() == null) {
            throw new IllegalArgumentException("用户不存在，请使用初始化脚本里的 demo 账号");
        }
        if (!hashPassword(password).equalsIgnoreCase(user.getPasswordHash())) {
            throw new IllegalArgumentException("密码错误");
        }

        return buildToken(user.getId());
    }

    /** 微信登录：MVP 阶段仅保留开发占位，正式小程序接入时再换成真实 code2Session */
    public Map<String, Object> wxLogin(String code) {
        String openId = "wx_" + (code != null && !code.isBlank() ? code : "dev");
        User user = userService.getByOpenId(openId);
        if (user == null) {
            user = new User();
            user.setUsername(openId);
            user.setOpenId(openId);
            user.setNickname("微信用户");
            user.setRole("girlfriend");
            user.setKitchenName("我们的厨房");
            user.setSignature("把每一餐都做成小小的约会 💗");
            user = userService.register(user);
        }
        return buildToken(user.getId());
    }

    /** 退出登录 */
    public void logout() {
        StpUtil.logout();
    }

    /** 刷新 token：单 token MVP 下重新签发当前登录用户 token */
    public Map<String, Object> refreshToken() {
        long userId = StpUtil.getLoginIdAsLong();
        return buildToken(userId);
    }

    private Map<String, Object> buildToken(Long userId) {
        StpUtil.login(userId);
        return Map.of(
                "token", StpUtil.getTokenValue(),
                "expiresIn", StpUtil.getTokenTimeout()
        );
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : encoded) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("密码算法不可用", e);
        }
    }
}
