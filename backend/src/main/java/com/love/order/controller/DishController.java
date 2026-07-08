package com.love.order.controller;

import com.love.order.common.R;
import com.love.order.entity.Dish;
import com.love.order.entity.DishCategory;
import com.love.order.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    /** 获取菜品分类列表 */
    @GetMapping("/categories")
    public R<List<DishCategory>> getCategories() {
        return R.ok(dishService.getCategories());
    }

    /** 获取菜品列表（支持分类筛选 + 关键字搜索） */
    @GetMapping("/list")
    public R<List<Dish>> getDishes(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return R.ok(dishService.getDishes(categoryId, keyword));
    }

    /** 按分类获取菜品 */
    @GetMapping("/category/{categoryId}")
    public R<List<Dish>> getDishesByCategory(@PathVariable Long categoryId) {
        return R.ok(dishService.getDishesByCategory(categoryId));
    }

    /** 搜索菜品 */
    @GetMapping("/search")
    public R<List<Dish>> searchDishes(@RequestParam String keyword) {
        return R.ok(dishService.getDishes(null, keyword));
    }
}
