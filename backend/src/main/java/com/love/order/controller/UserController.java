package com.love.order.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.love.order.common.R;
import com.love.order.entity.User;
import com.love.order.service.UserService;
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

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getNickname());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("role", user.getRole());
        result.put("kitchenName", user.getKitchenName());
        result.put("signature", user.getSignature());
        result.put("partnerId", user.getPartnerId());
        return R.ok(result);
    }

    /** 更新用户信息 */
    @PutMapping("/info")
    public R<Void> updateInfo(@RequestBody Map<String, Object> body) {
        long userId = StpUtil.getLoginIdAsLong();
        User user = new User();
        user.setId(userId);
        if (body.containsKey("nickname")) user.setNickname((String) body.get("nickname"));
        if (body.containsKey("avatar")) user.setAvatar((String) body.get("avatar"));
        if (body.containsKey("kitchenName")) user.setKitchenName((String) body.get("kitchenName"));
        if (body.containsKey("signature")) user.setSignature((String) body.get("signature"));
        if (body.containsKey("role")) user.setRole((String) body.get("role"));
        userService.updateUser(user);
        return R.ok();
    }

    /** 情侣绑定 */
    @PostMapping("/bind")
    public R<Void> bindPartner(@RequestBody Map<String, Object> body) {
        long userId = StpUtil.getLoginIdAsLong();
        Long partnerId = body.get("partnerId") != null
                ? ((Number) body.get("partnerId")).longValue()
                : null;
        if (partnerId == null) return R.fail("请提供对方ID");
        userService.bindPartner(userId, partnerId);
        return R.ok();
    }
}
