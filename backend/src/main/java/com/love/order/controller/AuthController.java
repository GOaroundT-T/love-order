package com.love.order.controller;

import com.love.order.common.R;
import com.love.order.dto.LoginRequest;
import com.love.order.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 账号密码登录 */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginRequest body) {
        return R.ok(authService.login(body.getUsername(), body.getPassword()));
    }

    /** 微信小程序登录（MVP 阶段保留占位） */
    @PostMapping("/wxLogin")
    public R<Map<String, Object>> wxLogin(@RequestBody Map<String, String> body) {
        return R.ok(authService.wxLogin(body.get("code")));
    }

    /** 刷新 token */
    @PostMapping("/refreshToken")
    public R<Map<String, Object>> refreshToken() {
        return R.ok(authService.refreshToken());
    }

    /** 退出登录 */
    @GetMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }
}
