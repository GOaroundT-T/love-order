package com.love.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜品分类 — 全部/荤菜/蔬菜/热汤/主食/小炒/饮品
 */
@Data
@TableName("t_dish_category")
public class DishCategory {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 排序 */
    private Integer sort;

    /** 图标（emoji 或图片） */
    private String icon;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
