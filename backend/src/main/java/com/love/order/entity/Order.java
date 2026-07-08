package com.love.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@TableName("t_order")
public class Order {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 下单用户 ID */
    private Long userId;

    /** 订单状态: pending/confirmed/cooking/finished/cancelled */
    private String status;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 备注 */
    private String remark;

    /** 情侣互动: 女友点菜后附言 */
    private String loveNote;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
