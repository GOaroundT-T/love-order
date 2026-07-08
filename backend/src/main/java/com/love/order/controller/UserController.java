package com.love.order.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.love.order.common.R;
import com.love.order.dto.BindPartnerRequest;
import com.love.order.dto.UpdateProfileRequest;
import com.love.order.entity.User;
import com.love.order.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 获取当前用户信息 */
    @GetMapping("/info")
    public R<Map<String, Object>> getInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) return R.fail("用户不存在");
        return R.ok(toProfile(user));
    }

    /** 更新用户信息 */
    @PutMapping("/info")
    public R<Void> updateInfo(@RequestBody UpdateProfileRequest body) {
        long userId = StpUtil.getLoginIdAsLong();
        User user = new User();
        user.setId(userId);
        user.setNickname(body.getNickname());
        user.setAvatar(body.getAvatar());
        user.setKitchenName(body.getKitchenName());
        user.setSignature(body.getSignature());
        userService.updateUser(user);
        return R.ok();
    }

    /** 情侣绑定 */
    @PostMapping("/bind")
    public R<Void> bindPartner(@Valid @RequestBody BindPartnerRequest body) {
        long userId = StpUtil.getLoginIdAsLong();
        userService.bindPartner(userId, body.getPartnerId());
        return R.ok();
    }

    private Map<String, Object> toProfile(User user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("role", user.getRole());
        result.put("kitchenName", user.getKitchenName());
        result.put("signature", user.getSignature());
        result.put("partnerId", user.getPartnerId());
        return result;
    }
}
