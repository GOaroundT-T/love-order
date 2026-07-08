<script lang="ts" setup>
import type { CartItem, Dish, DishCategory, SpicyLevel } from '@/types/dish'
import { getCategories, getDishes } from '@/api/dish'
import { createOrder } from '@/api/order'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'
import { useTokenStore } from '@/store/token'

defineOptions({ name: 'Kitchen' })
definePage({ type: 'home', style: { navigationStyle: 'custom', navigationBarTitleText: '我们的厨房' } })

const categories = ref<DishCategory[]>([])
const activeCategoryId = ref<number>(0)
const dishes = ref<Dish[]>([])
const keyword = ref('')
const cartStore = useCartStore()
const userStore = useUserStore()
const tokenStore = useTokenStore()
const cartVisible = ref(false)
const submitting = ref(false)
const loveNote = ref('今天想吃这些，辛苦我的专属大厨啦 💗')

const kitchenName = computed(() => userStore.userInfo.kitchenName || '我们的暖心厨房')
const signature = computed(() => userStore.userInfo.signature || '只做给你一个人的私房菜 💗')
const partnerText = computed(() => userStore.hasPartner ? '已绑定另一半' : '还没绑定 TA')
const todayPick = computed(() => dishes.value.find(d => d.rating >= 5) || dishes.value[0])

async function loadData() {
  const [catRes, dishRes] = await Promise.all([getCategories(), getDishes()])
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

function addDish(dish: Dish) {
  if (dish.onShelf === false) return uni.showToast({ title: '这道菜今天休息啦', icon: 'none' })
  cartStore.addItem({ id: dish.id, name: dish.name, image: dish.image || '', price: dish.price })
}

function removeCartItem(item: CartItem) {
  cartStore.decreaseItem(item.dishId)
}

async function submitOrder() {
  if (!tokenStore.updateNowTime().hasLogin) {
    return uni.navigateTo({ url: `/pages/auth/login?redirect=${encodeURIComponent('/pages/index/index')}` })
  }
  if (cartStore.items.length === 0) return
  submitting.value = true
  try {
    await createOrder({
      items: cartStore.items.map(i => ({ dishId: i.dishId, quantity: i.quantity })),
      remark: '情侣私房菜点单',
      loveNote: loveNote.value,
    })
    cartStore.clearCart()
    cartVisible.value = false
    loveNote.value = '今天想吃这些，辛苦我的专属大厨啦 💗'
    uni.showToast({ title: '爱心点单已送达', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/order/list' }), 700)
  }
  catch { /* 拦截器已 toast */ }
  finally { submitting.value = false }
}

let searchTimer: ReturnType<typeof setTimeout>
function onSearchInput(val: string) {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    keyword.value = val
    loadDishes(activeCategoryId.value || undefined)
  }, 300)
}

function goBindPartner() {
  if (!tokenStore.updateNowTime().hasLogin) return uni.navigateTo({ url: '/pages/auth/login?redirect=/pages/me/me' })
  uni.switchTab({ url: '/pages/me/me' })
}

function goOrders() {
  if (!tokenStore.updateNowTime().hasLogin) return uni.navigateTo({ url: '/pages/auth/login?redirect=/pages/order/list' })
  uni.switchTab({ url: '/pages/order/list' })
}

function getStars(rating: number): string {
  return '★'.repeat(rating || 0) + '☆'.repeat(Math.max(0, 5 - (rating || 0)))
}

function spicyText(level?: SpicyLevel) {
  const map: Record<string, string> = { none: '不辣', mild: '微辣', medium: '中辣', hot: '很辣' }
  return map[level || 'none'] || '家常味'
}

onLoad(async () => {
  if (tokenStore.updateNowTime().hasLogin) userStore.fetchUserInfo().catch(() => {})
  await loadData()
})
</script>

<template>
  <view class="kitchen-page love-page flex flex-col h-full">
    <view class="top-banner love-hero warm-gradient px-5 pt-safe pb-5" style="padding-top: calc(env(safe-area-inset-top) + 36rpx);">
      <view class="flex items-center gap-4 relative z-1">
        <view class="avatar-pair shrink-0">
          <view class="avatar-pair__item center text-6">👨‍🍳</view>
          <view class="avatar-pair__item center text-6">👩</view>
        </view>
        <view class="flex-1 min-w-0">
          <view class="love-pill mb-2">💌 {{ partnerText }}</view>
          <view class="text-5.5 font-800 text-#4a3728 truncate">{{ kitchenName }}</view>
          <view class="text-3 text-#8b7355 mt-1 truncate">{{ signature }}</view>
        </view>
        <view class="bell-btn center" @tap="goOrders">
          <wd-icon name="bell" size="20px" color="#e85d3a" />
        </view>
      </view>
      <view v-if="todayPick" class="today-card relative z-1 mt-4" @tap="addDish(todayPick)">
        <view>
          <view class="text-2.8 text-#a08c7a">今日想把这道做给你</view>
          <view class="text-4 font-700 text-#4a3728 mt-1">{{ todayPick.name }}</view>
        </view>
        <view class="love-chip">点一份</view>
      </view>
    </view>

    <view class="search-bar px-4 py-3 flex items-center gap-3 bg-#fffaf7">
      <view class="flex-1 search-input flex items-center bg-white rounded-6 px-3 h-10">
        <wd-icon name="search" size="16px" color="#b8a99a" />
        <input class="flex-1 ml-2 text-3.5 text-#4a3728 border-none outline-none bg-transparent" placeholder="今天想被投喂什么？" placeholder-style="color:#c5b8ad" :value="keyword" @input="($event: any) => onSearchInput($event.detail.value)" />
      </view>
      <view class="action-btn love-chip shrink-0" @tap="goBindPartner">情侣绑定</view>
    </view>

    <view class="menu-body flex-1 flex overflow-hidden">
      <scroll-view scroll-y class="category-side bg-#fff7f1 shrink-0">
        <view v-for="cat in [{ id: 0, name: '全部', icon: '🍽️' } as DishCategory, ...categories]" :key="cat.id" class="category-item" :class="{ active: activeCategoryId === cat.id }" @tap="switchCategory(cat.id)">
          <text class="text-5 mb-1">{{ cat.icon || '🍽️' }}</text>
          <text class="text-3.2">{{ cat.name }}</text>
        </view>
        <view class="h-10" />
      </scroll-view>

      <scroll-view scroll-y class="dish-list flex-1 min-w-0 px-3">
        <view v-if="dishes.length === 0" class="empty-state">
          <view class="text-8 mb-2">🍳</view>
          <text class="text-3.5">还没有找到想吃的菜</text>
        </view>
        <view v-for="dish in dishes" :key="dish.id" class="dish-card love-card mt-3 p-3 flex" :class="{ 'is-off': dish.onShelf === false }">
          <view v-if="dish.image" class="dish-thumb overflow-hidden bg-#f0ebe6">
            <image :src="dish.image" mode="aspectFill" class="w-full h-full" />
          </view>
          <view v-else class="dish-thumb bg-#fff5f0 center text-8">🍽️</view>
          <view class="dish-info flex-1 ml-3 flex flex-col justify-between min-w-0">
            <view>
              <view class="flex items-center gap-2">
                <view class="flex-1 min-w-0 text-4 font-700 text-#4a3728 truncate">{{ dish.name }}</view>
                <view class="love-chip shrink-0">{{ spicyText(dish.spicyLevel) }}</view>
              </view>
              <view class="text-2.8 text-#a08c7a mt-1 truncate-2">{{ dish.description }}</view>
            </view>
            <view class="flex items-center justify-between mt-2">
              <view class="min-w-0">
                <view class="dish-meta flex items-center gap-1">
                  <text class="price text-4.5">¥{{ dish.price }}</text>
                  <text class="text-2.5 text-#f0ad4e">{{ getStars(dish.rating) }}</text>
                </view>
                <view class="text-2.5 text-#c59b8a mt-1">想给 TA 做</view>
              </view>
              <view class="add-btn w-8 h-8 rounded-full bg-#e85d3a flex items-center justify-center shrink-0" @tap="addDish(dish)">
                <wd-icon name="plus" size="18px" color="#fff" />
              </view>
            </view>
          </view>
        </view>
        <view class="h-32" />
      </scroll-view>
    </view>

    <view class="cart-bar" :class="{ 'has-items': cartStore.totalCount > 0 }" @tap="cartStore.totalCount > 0 ? (cartVisible = true) : null">
      <wd-badge :model-value="cartStore.totalCount" :hidden="cartStore.totalCount === 0">
        <view class="cart-icon-wrap w-12 h-12 rounded-full flex items-center justify-center" :class="cartStore.totalCount > 0 ? 'bg-#e85d3a' : 'bg-#d4ccc5'">
          <wd-icon name="cart" size="24px" :color="cartStore.totalCount > 0 ? '#fff' : '#aaa'" />
        </view>
      </wd-badge>
      <view class="flex-1 ml-3">
        <text v-if="cartStore.totalCount > 0" class="price text-4.5">¥{{ cartStore.totalAmount.toFixed(2) }}</text>
        <text v-else class="text-3.5 text-#bbb">还没点菜，挑几道让 TA 做吧~</text>
      </view>
      <view v-if="cartStore.totalCount > 0" class="cart-action love-chip shrink-0">写纸条并送出</view>
    </view>

    <wd-popup v-model="cartVisible" position="bottom" :z-index="1201" custom-style="border-radius: 32rpx 32rpx 0 0; max-height: 76vh;">
      <view class="cart-popup-panel p-5 bg-#fffaf7">
        <view class="flex items-center justify-between mb-4">
          <view>
            <text class="text-4.5 font-800 text-#4a3728 block">爱心购物车</text>
            <text class="text-2.8 text-#a08c7a">把想吃的和想说的都交给 TA</text>
          </view>
          <wd-icon name="close" size="20px" color="#999" @click="cartVisible = false" />
        </view>

        <scroll-view scroll-y class="cart-list" style="max-height: 34vh;">
          <view v-for="item in cartStore.items" :key="item.dishId" class="flex items-center justify-between py-3 border-b border-#f0ebe6">
            <view class="flex items-center gap-2 flex-1 min-w-0">
              <view v-if="item.dishImage" class="w-12 h-12 rounded-lg shrink-0 overflow-hidden bg-#f0ebe6"><image :src="item.dishImage" mode="aspectFill" class="w-full h-full" /></view>
              <view v-else class="w-12 h-12 rounded-lg shrink-0 bg-#fff5f0 center text-7">🍽️</view>
              <view class="min-w-0 flex-1">
                <text class="text-3.5 font-600 text-#4a3728 truncate block">{{ item.dishName }}</text>
                <text class="price text-3">¥{{ (item.price * item.quantity).toFixed(2) }}</text>
              </view>
            </view>
            <view class="flex items-center gap-2 shrink-0">
              <view class="qty-btn border border-#ddd" @tap="removeCartItem(item)"><wd-icon name="minus" size="14px" color="#999" /></view>
              <text class="text-3.5 w-6 text-center">{{ item.quantity }}</text>
              <view class="qty-btn bg-#e85d3a" @tap="addDish({ id: item.dishId, name: item.dishName, image: item.dishImage, price: item.price, rating: 5 } as Dish)"><wd-icon name="plus" size="14px" color="#fff" /></view>
            </view>
          </view>
        </scroll-view>

        <view class="love-note mt-4">
          <text class="text-3 font-700 text-#4a3728 block mb-2">给 TA 的小纸条</text>
          <textarea v-model="loveNote" :maxlength="120" auto-height placeholder="比如：今天想吃热乎乎的，也想你抱抱我" placeholder-style="color:#c9b8aa" />
        </view>

        <view class="submit-wrap">
          <wd-button block size="large" type="primary" :loading="submitting" :disabled="cartStore.items.length === 0" @click="submitOrder" custom-style="background: #e85d3a; border-radius: 28rpx; height: 92rpx; font-size: 30rpx;">
            送出爱心点单 (¥{{ cartStore.totalAmount.toFixed(2) }})
          </wd-button>
        </view>
      </view>
    </wd-popup>
  </view>
</template>

<style lang="scss" scoped>
.kitchen-page { height: 100vh; overflow: hidden; }
.bell-btn { width: 72rpx; height: 72rpx; border-radius: 999rpx; background: rgba(255,255,255,0.72); }
.today-card { display: flex; align-items: center; justify-content: space-between; padding: 22rpx 24rpx; border-radius: 28rpx; background: rgba(255,255,255,0.72); box-shadow: 0 10rpx 28rpx rgba(126,74,45,.08); }
.search-bar { border-bottom: 1rpx solid #f0ebe6; }
.search-input { box-shadow: inset 0 0 0 1rpx #f0ebe6; }
.menu-body { min-height: 0; }
.category-side { width: 144rpx; }
.category-side .category-item { min-height: 104rpx; padding: 18rpx 10rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #8b7355; position: relative; transition: all .2s; }
.category-side .category-item.active { color: #e85d3a; font-weight: 800; background: #fff; }
.category-side .category-item.active::before { content: ''; position: absolute; left: 0; top: 26rpx; bottom: 26rpx; width: 6rpx; background: #e85d3a; border-radius: 0 8rpx 8rpx 0; }
.dish-list { width: 0; min-width: 0; box-sizing: border-box; }
.dish-list ::-webkit-scrollbar { display: none; }
.dish-card { width: 100%; min-width: 0; overflow: hidden; box-sizing: border-box; transition: transform .15s; }
.dish-card:active { transform: scale(.985); }
.dish-card.is-off { opacity: .55; }
.dish-thumb { width: 128rpx; height: 128rpx; border-radius: 24rpx; flex-shrink: 0; }
.dish-info { overflow: hidden; }
.dish-meta { min-width: 0; flex-wrap: wrap; row-gap: 2rpx; }
.add-btn:active, .action-btn:active { opacity: .82; }
.cart-bar { position: relative; z-index: 20; flex-shrink: 0; display: flex; align-items: center; padding: 16rpx 24rpx; padding-bottom: calc(16rpx + env(safe-area-inset-bottom)); background: rgba(255,255,255,.96); border-top: 1rpx solid #f0ebe6; }
.cart-bar.has-items { box-shadow: 0 -8rpx 24rpx rgba(126,74,45,.08); }
.cart-icon-wrap { transition: all .3s; margin-top: -16rpx; }
.cart-action { justify-content: center; min-width: 140rpx; }
.qty-btn { width: 48rpx; height: 48rpx; border-radius: 999rpx; display: flex; align-items: center; justify-content: center; }
.cart-popup-panel { padding-bottom: calc(40rpx + 50px + env(safe-area-inset-bottom)); }
.love-note { padding: 22rpx; border-radius: 24rpx; background: #fff; border: 1rpx solid #f0ebe6; }
.love-note textarea { width: 100%; min-height: 88rpx; color: #4a3728; font-size: 28rpx; line-height: 1.6; }
.submit-wrap { margin-top: 32rpx; }
</style>
