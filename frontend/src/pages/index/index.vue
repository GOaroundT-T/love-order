<script lang="ts" setup>
import type { CartItem, Dish, DishCategory } from '@/types/dish'
import { getCategories, getDishes } from '@/api/dish'
import { createOrder } from '@/api/order'
import { useCartStore } from '@/store/cart'

defineOptions({ name: 'Kitchen' })
definePage({ type: 'home', style: { navigationStyle: 'custom', navigationBarTitleText: '鱼的厨房' } })

// ---------- 数据 ----------
const categories = ref<DishCategory[]>([])
const activeCategoryId = ref<number>(0) // 0 = 全部
const dishes = ref<Dish[]>([])
const keyword = ref('')
const cartStore = useCartStore()
const cartVisible = ref(false)
const submitting = ref(false)

// ---------- 加载 ----------
async function loadData() {
  const [catRes, dishRes] = await Promise.all([
    getCategories(),
    getDishes(),
  ])
  categories.value = catRes || []
  dishes.value = dishRes || []
}

function switchCategory(id: number) {
  activeCategoryId.value = id
  loadDishes(id)
}

async function loadDishes(categoryId?: number) {
  dishes.value = await getDishes(categoryId || undefined, keyword.value || undefined)
}

// ---------- 购物车 ----------
function addDish(dish: Dish) {
  cartStore.addItem({ id: dish.id, name: dish.name, image: dish.image, price: dish.price })
}

function removeCartItem(item: CartItem) {
  cartStore.decreaseItem(item.dishId)
}

async function submitOrder() {
  if (cartStore.items.length === 0) return
  submitting.value = true
  try {
    await createOrder({
      items: cartStore.items.map(i => ({ dishId: i.dishId, quantity: i.quantity })),
      remark: '',
      loveNote: '💕 想吃这些~~',
    })
    cartStore.clearCart()
    cartVisible.value = false
    uni.showToast({ title: '已下单，等着吃吧~', icon: 'success' })
  }
  catch { /* 拦截器已 toast */ }
  finally { submitting.value = false }
}

// ---------- 搜索 ----------
let searchTimer: any
function onSearchInput(val: string) {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    keyword.value = val
    loadDishes(activeCategoryId.value || undefined)
  }, 300)
}

function onManageMenu() {
  uni.showToast({ title: '管理菜单（开发中）', icon: 'none' })
}

function onInviteOrder() {
  uni.showToast({ title: '邀友点菜（开发中）', icon: 'none' })
}

// ---------- 评分星星 ----------
function getStars(rating: number): string {
  return '★'.repeat(rating) + '☆'.repeat(5 - rating)
}

onLoad(() => { loadData() })
</script>

<template>
  <view class="kitchen-page flex flex-col h-full">
    <!-- ====== 顶部 banner ====== -->
    <view class="top-banner warm-gradient px-6 pt-safe flex items-center gap-4" style="padding-top: calc(env(safe-area-inset-top) + 40rpx); padding-bottom: 24rpx;">
      <view class="flex items-center gap-2">
        <view class="avatars flex">
          <view class="avatar-wrap w-12 h-12 rounded-full overflow-hidden border-2 border-white bg-#f0ebe6 center">
            <text class="text-6">👨‍🍳</text>
          </view>
          <view class="avatar-wrap w-12 h-12 rounded-full overflow-hidden border-2 border-white -ml-3 bg-#f0ebe6 center">
            <text class="text-6">👩</text>
          </view>
        </view>
      </view>
      <view class="flex-1 min-w-0">
        <view class="text-5 font-700 text-#4a3728">鱼的厨房</view>
        <view class="text-3 text-#a08c7a mt-1">只做给你一个人的私房菜 💗</view>
      </view>
      <wd-icon name="bell" size="22px" color="#a08c7a" />
    </view>

    <!-- ====== 搜索栏 + 按钮 ====== -->
    <view class="search-bar px-4 py-3 flex items-center gap-3 bg-white">
      <view class="flex-1 search-input flex items-center bg-#f5f2ee rounded-xl px-3 h-9">
        <wd-icon name="search" size="16px" color="#b8a99a" />
        <input
          class="flex-1 ml-2 text-3.5 text-#4a3728 border-none outline-none bg-transparent"
          placeholder="想吃什么？"
          placeholder-style="color:#c5b8ad"
          :value="keyword"
          @input="($event: any) => onSearchInput($event.detail.value)"
        />
      </view>
      <view class="flex gap-2 shrink-0">
        <view class="action-btn text-3 text-#8b7355 bg-#f5f2ee rounded-xl px-3 h-9 flex items-center gap-1" @tap="onInviteOrder">
          <wd-icon name="user" size="14px" color="#8b7355" />
          <text>邀友点菜</text>
        </view>
        <view class="action-btn text-3 text-#8b7355 bg-#f5f2ee rounded-xl px-3 h-9 flex items-center gap-1" @tap="onManageMenu">
          <wd-icon name="setting1" size="14px" color="#8b7355" />
          <text>管理菜单</text>
        </view>
      </view>
    </view>

    <!-- ====== 左右分栏 ====== -->
    <view class="flex-1 flex overflow-hidden">
      <!-- 左侧分类 -->
      <scroll-view scroll-y class="category-side w-40 bg-#faf7f4 shrink-0">
        <view
          v-for="cat in [{ id: 0, name: '全部', icon: '🍽️', sort: 0 } as DishCategory, ...categories]"
          :key="cat.id"
          class="category-item"
          :class="{ active: activeCategoryId === cat.id }"
          @tap="switchCategory(cat.id)"
        >
          <text class="text-5 mr-1">{{ cat.icon || '' }}</text>
          <text class="text-3.5">{{ cat.name }}</text>
        </view>
        <!-- 底部占位 -->
        <view class="h-10" />
      </scroll-view>

      <!-- 右侧菜品列表 -->
      <scroll-view scroll-y class="dish-list flex-1 px-3" :scroll-into-view="'cat-' + activeCategoryId">
        <view v-if="dishes.length === 0" class="center pt-20 text-#b8a99a">
          <view class="text-6 mb-2">🍳</view>
          <text class="text-3.5">还没有菜品哦</text>
        </view>
        <view v-for="dish in dishes" :key="dish.id" class="dish-card card mt-3 p-3 flex">
          <view v-if="dish.image" class="w-22 h-22 rounded-xl shrink-0 overflow-hidden bg-#f0ebe6">
            <image :src="dish.image" mode="aspectFill" class="w-full h-full" />
          </view>
          <view v-else class="w-22 h-22 rounded-xl shrink-0 bg-#fff5f0 center text-9">
            🍽️
          </view>
          <view class="flex-1 ml-3 flex flex-col justify-between min-w-0">
            <view>
              <view class="text-4 font-600 text-#4a3728 truncate">{{ dish.name }}</view>
              <view class="text-2.5 text-#a08c7a mt-1 truncate-2">{{ dish.description }}</view>
            </view>
            <view class="flex items-center justify-between mt-2">
              <view class="flex items-center gap-1">
                <text class="price text-4.5">¥{{ dish.price }}</text>
                <text class="text-2.5 text-#f0ad4e">{{ getStars(dish.rating) }}</text>
              </view>
              <view class="add-btn w-7 h-7 rounded-full bg-#e85d3a flex items-center justify-center" @tap="addDish(dish)">
                <wd-icon name="plus" size="18px" color="#fff" />
              </view>
            </view>
          </view>
        </view>
        <view class="h-30" />
      </scroll-view>
    </view>

    <!-- ====== 底部购物车浮层 ====== -->
    <view class="cart-bar" :class="{ 'has-items': cartStore.totalCount > 0 }" @tap="cartStore.totalCount > 0 ? (cartVisible = true) : null">
      <wd-badge :model-value="cartStore.totalCount" :hidden="cartStore.totalCount === 0">
        <view class="cart-icon-wrap w-12 h-12 rounded-full flex items-center justify-center" :class="cartStore.totalCount > 0 ? 'bg-#e85d3a' : 'bg-#d4ccc5'">
          <wd-icon name="cart" size="24px" :color="cartStore.totalCount > 0 ? '#fff' : '#aaa'" />
        </view>
      </wd-badge>
      <text v-if="cartStore.totalCount > 0" class="price text-4.5 ml-3">¥{{ cartStore.totalAmount.toFixed(2) }}</text>
      <text v-else class="text-3.5 text-#bbb ml-3">没有菜品，去点几个吧~</text>
    </view>

    <!-- ====== 购物车弹窗 ====== -->
    <wd-popup v-model="cartVisible" position="bottom" custom-style="border-radius: 24rpx 24rpx 0 0; max-height: 60vh;">
      <view class="p-5 pb-safe">
        <view class="flex items-center justify-between mb-4">
          <text class="text-4.5 font-700 text-#4a3728">购物车</text>
          <wd-icon name="close" size="20px" color="#999" @click="cartVisible = false" />
        </view>

        <scroll-view scroll-y class="cart-list" style="max-height: 40vh;">
          <view v-for="item in cartStore.items" :key="item.dishId" class="flex items-center justify-between py-3 border-b border-#f0ebe6">
            <view class="flex items-center gap-2 flex-1 min-w-0">
              <view v-if="item.dishImage" class="w-12 h-12 rounded-lg shrink-0 overflow-hidden bg-#f0ebe6">
                <image :src="item.dishImage" mode="aspectFill" class="w-full h-full" />
              </view>
              <view v-else class="w-12 h-12 rounded-lg shrink-0 bg-#fff5f0 center text-7">
                🍽️
              </view>
              <view class="min-w-0 flex-1">
                <text class="text-3.5 font-500 text-#4a3728 truncate block">{{ item.dishName }}</text>
                <text class="price text-3">¥{{ (item.price * item.quantity).toFixed(2) }}</text>
              </view>
            </view>
            <view class="flex items-center gap-2 shrink-0">
              <view class="w-6 h-6 rounded-full border border-#ddd flex items-center justify-center" @tap="removeCartItem(item)">
                <wd-icon name="minus" size="14px" color="#999" />
              </view>
              <text class="text-3.5 w-6 text-center">{{ item.quantity }}</text>
              <view class="w-6 h-6 rounded-full bg-#e85d3a flex items-center justify-center" @tap="addDish({ id: item.dishId, name: item.dishName, image: item.dishImage, price: item.price } as Dish)">
                <wd-icon name="plus" size="14px" color="#fff" />
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="mt-4">
          <wd-button block size="large" type="primary" :loading="submitting" :disabled="cartStore.items.length === 0" @click="submitOrder" custom-style="background: #e85d3a; border-radius: 24rpx; height: 88rpx; font-size: 30rpx;">
            下单 (¥{{ cartStore.totalAmount.toFixed(2) }})
          </wd-button>
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<style lang="scss" scoped>
.kitchen-page {
  height: 100vh;
  overflow: hidden;
  background: #fef9f5;
}

.search-bar {
  border-bottom: 1rpx solid #f0ebe6;
}

.category-side {
  .category-item {
    padding: 24rpx 20rpx;
    display: flex;
    align-items: center;
    color: #8b7355;
    font-size: 28rpx;
    position: relative;
    transition: all 0.2s;

    &.active {
      color: #e85d3a;
      font-weight: 700;
      background: #fff;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 6rpx;
        height: 32rpx;
        background: #e85d3a;
        border-radius: 0 4rpx 4rpx 0;
      }
    }
  }
}

.dish-list {
  ::-webkit-scrollbar { display: none; }
}

.dish-card {
  transition: transform 0.15s;
  &:active { transform: scale(0.98); }
}

.add-btn:active {
  opacity: 0.8;
}

.cart-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #f0ebe6;

  &.has-items {
    box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.06);
  }

  .cart-icon-wrap {
    transition: all 0.3s;
    margin-top: -16rpx;
  }
}

.truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
