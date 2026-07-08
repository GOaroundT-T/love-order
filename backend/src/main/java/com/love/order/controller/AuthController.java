package com.love.order.controller;

import com.love.order.common.R;
import com.love.order.service.AuthService;
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
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        return R.ok(authService.login(username, password));
    }

    /** 微信小程序登录 */
    @PostMapping("/wxLogin")
    public R<Map<String, Object>> wxLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        return R.ok(authService.wxLogin(code));
    }

    /** 刷新 token */
    @PostMapping("/refreshToken")
    public R<Map<String, Object>> refreshToken(@RequestBody Map<String, String> body) {
        return R.ok(authService.refreshToken());
    }

    /** 退出登录 */
    @GetMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }
}
