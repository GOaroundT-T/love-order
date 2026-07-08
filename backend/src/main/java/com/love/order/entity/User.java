package com.love.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 — 情侣双方（你 + 女朋友）
 */
@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 登录用户名 */
    private String username;

    /** SHA-256 密码哈希（MVP 阶段） */
    private String passwordHash;

    /** 微信 openId */
    private String openId;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 角色: chef(厨师/你) / girlfriend(女朋友) */
    private String role;

    /** 厨房名称（如 "鱼的厨房"） */
    private String kitchenName;

    /** 签名 */
    private String signature;

    /** 情侣绑定对方的 userId */
    private Long partnerId;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
