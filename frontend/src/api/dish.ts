import type { Dish, DishCategory } from '@/types/dish'
import { http } from '@/http/http'

/** 获取菜品分类列表 */
export function getCategories() {
  return http.get<DishCategory[]>('/dish/categories')
}

/** 获取菜品列表（按分类） */
export function getDishes(categoryId?: number, keyword?: string) {
  return http.get<Dish[]>('/dish/list', { categoryId, keyword })
}

/** 根据分类获取菜品 */
export function getDishesByCategory(categoryId: number) {
  return http.get<Dish[]>(`/dish/category/${categoryId}`)
}

/** 搜索菜品 */
export function searchDishes(keyword: string) {
  return http.get<Dish[]>('/dish/search', { keyword })
}
