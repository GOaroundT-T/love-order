package com.love.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项 — 订单中的单个菜品
 */
@Data
@TableName("t_order_item")
public class OrderItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 订单 ID */
    private Long orderId;

    /** 菜品 ID */
    private Long dishId;

    /** 菜品名称（冗余，方便展示） */
    private String dishName;

    /** 菜品图片 */
    private String dishImage;

    /** 单价 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
