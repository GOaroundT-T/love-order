package com.love.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.love.order.entity.User;
import com.love.order.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    /** 根据用户名查找 */
    public User getByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getNickname, username)
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

    /** 更新用户信息 */
    public void updateUser(User user) {
        userMapper.updateById(user);
    }

    /** 情侣绑定 */
    public void bindPartner(Long userId, Long partnerId) {
        User user = new User();
        user.setId(userId);
        user.setPartnerId(partnerId);
        userMapper.updateById(user);

        // 双向绑定
        User partner = new User();
        partner.setId(partnerId);
        partner.setPartnerId(userId);
        userMapper.updateById(partner);
    }
}
