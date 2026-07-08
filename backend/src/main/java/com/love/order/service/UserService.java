package com.love.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.love.order.entity.User;
import com.love.order.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /** 根据 openId 查找 */
    public User getByOpenId(String openId) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenId, openId)
        );
    }

    /** 根据登录用户名查找 */
    public User getByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
    }

    /** 注册新用户 */
    public User register(User user) {
        userMapper.insert(user);
        return user;
    }

    /** 获取用户信息 */
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }

    /** 更新用户信息：只更新资料字段 */
    public void updateUser(User user) {
        User update = new User();
        update.setId(user.getId());
        update.setNickname(blankToNull(user.getNickname()));
        update.setAvatar(blankToNull(user.getAvatar()));
        update.setKitchenName(blankToNull(user.getKitchenName()));
        update.setSignature(blankToNull(user.getSignature()));
        userMapper.updateById(update);
    }

    /** 情侣绑定 */
    @Transactional
    public void bindPartner(Long userId, Long partnerId) {
        if (partnerId == null) throw new IllegalArgumentException("请提供对方ID");
        if (userId.equals(partnerId)) throw new IllegalArgumentException("不能绑定自己哦");

        User user = userMapper.selectById(userId);
        User partner = userMapper.selectById(partnerId);
        if (user == null) throw new IllegalArgumentException("当前用户不存在");
        if (partner == null) throw new IllegalArgumentException("对方用户不存在，请检查ID");

        if (user.getPartnerId() != null && !user.getPartnerId().equals(partnerId)) {
            throw new IllegalArgumentException("你已经绑定了另一半，不能重复绑定");
        }
        if (partner.getPartnerId() != null && !partner.getPartnerId().equals(userId)) {
            throw new IllegalArgumentException("对方已经绑定了另一半");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPartnerId(partnerId);
        userMapper.updateById(updateUser);

        User updatePartner = new User();
        updatePartner.setId(partnerId);
        updatePartner.setPartnerId(userId);
        userMapper.updateById(updatePartner);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
