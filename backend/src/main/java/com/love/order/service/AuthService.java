package com.love.order.service;

import cn.dev33.satoken.stp.StpUtil;
import com.love.order.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    /** 账号密码登录 */
    public Map<String, Object> login(String username, String password) {
        // 开发阶段：模拟登录，只要密码是 123456 就通过
        if (password == null || !"123456".equals(password)) {
            throw new IllegalArgumentException("密码错误");
        }

        User user = userService.getByUsername(username);
        if (user == null) {
            // 自动注册
            user = new User();
            user.setNickname(username);
            user.setRole(username.contains("girl") || username.contains("女") ? "girlfriend" : "chef");
            user.setKitchenName(username + "的厨房");
            user.setSignature("只做给你一个人的私房菜 💗");
            user = userService.register(user);
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        return Map.of(
                "token", token,
                "expiresIn", StpUtil.getTokenTimeout()
        );
    }

    /** 微信登录 */
    public Map<String, Object> wxLogin(String code) {
        // 开发阶段：模拟微信登录，code 作为 openId
        String openId = "wx_" + (code != null ? code : "dev");
        User user = userService.getByOpenId(openId);
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickname("微信用户");
            user.setRole("girlfriend");
            user.setKitchenName("我的厨房");
            user.setSignature("只做给你一个人的私房菜 💗");
            user = userService.register(user);
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        return Map.of(
                "token", token,
                "expiresIn", StpUtil.getTokenTimeout()
        );
    }

    /** 退出登录 */
    public void logout() {
        StpUtil.logout();
    }

    /** 刷新 token */
    public Map<String, Object> refreshToken() {
        long userId = StpUtil.getLoginIdAsLong();
        StpUtil.login(userId);
        String token = StpUtil.getTokenValue();

        return Map.of(
                "token", token,
                "expiresIn", StpUtil.getTokenTimeout()
        );
    }
}
