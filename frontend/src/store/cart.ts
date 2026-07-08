import type { CartItem } from '@/types/dish'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useCartStore = defineStore(
  'cart',
  () => {
    const items = ref<CartItem[]>([])

    /** 购物车总数 */
    const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))

    /** 总金额 */
    const totalAmount = computed(() =>
      items.value.reduce((sum, item) => sum + item.price * item.quantity, 0),
    )

    /** 添加菜品到购物车 */
    function addItem(dish: { id: number; name: string; image: string; price: number }) {
      const exist = items.value.find(i => i.dishId === dish.id)
      if (exist) {
        exist.quantity++
      }
      else {
        items.value.push({
          dishId: dish.id,
          dishName: dish.name,
          dishImage: dish.image,
          price: dish.price,
          quantity: 1,
        })
      }
      uni.showToast({ title: '已加入购物车', icon: 'success', duration: 800 })
    }

    /** 减少数量 */
    function decreaseItem(dishId: number) {
      const idx = items.value.findIndex(i => i.dishId === dishId)
      if (idx === -1) return
      if (items.value[idx].quantity <= 1) {
        items.value.splice(idx, 1)
      }
      else {
        items.value[idx].quantity--
      }
    }

    /** 删除菜品 */
    function removeItem(dishId: number) {
      items.value = items.value.filter(i => i.dishId !== dishId)
    }

    /** 清空购物车 */
    function clearCart() {
      items.value = []
    }

    return { items, totalCount, totalAmount, addItem, decreaseItem, removeItem, clearCart }
  },
  { persist: true },
)
