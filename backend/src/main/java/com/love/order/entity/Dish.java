package com.love.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品
 */
@Data
@TableName("t_dish")
public class Dish {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 菜品名称 */
    private String name;

    /** 分类 ID */
    private Long categoryId;

    /** 菜品图片 */
    private String image;

    /** 价格 */
    private BigDecimal price;

    /** 评分 1-5 */
    private Integer rating;

    /** 描述 */
    private String description;

    /** 辣度: none/mild/medium/hot */
    private String spicyLevel;

    /** 上架: true */
    private Boolean onShelf;

    /** 排序 */
    private Integer sort;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
