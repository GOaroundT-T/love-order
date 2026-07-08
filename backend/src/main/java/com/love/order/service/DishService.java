package com.love.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.love.order.entity.Dish;
import com.love.order.entity.DishCategory;
import com.love.order.mapper.DishCategoryMapper;
import com.love.order.mapper.DishMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishMapper dishMapper;
    private final DishCategoryMapper dishCategoryMapper;

    /** 获取所有分类 */
    public List<DishCategory> getCategories() {
        return dishCategoryMapper.selectList(
                new LambdaQueryWrapper<DishCategory>().orderByAsc(DishCategory::getSort)
        );
    }

    /** 获取菜品列表（支持分类筛选 + 关键字搜索） */
    public List<Dish> getDishes(Long categoryId, String keyword) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getOnShelf, true)
                .orderByAsc(Dish::getSort);

        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(Dish::getName, keyword)
                    .or()
                    .like(Dish::getDescription, keyword));
        }
        return dishMapper.selectList(wrapper);
    }

    /** 按分类获取菜品 */
    public List<Dish> getDishesByCategory(Long categoryId) {
        return getDishes(categoryId, null);
    }
}
