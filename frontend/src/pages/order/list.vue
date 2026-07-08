<script lang="ts" setup>
import type { LoveOrder, OrderStatus } from '@/types/dish'
import { OrderStatusColor, OrderStatusText } from '@/types/dish'
import { getOrders, updateOrderStatus } from '@/api/order'
import { useTokenStore } from '@/store/token'
import { setOrderSocketMessageHandler } from '@/utils/ws'
import { onShow } from '@dcloudio/uni-app'

defineOptions({ name: 'OrderList' })
definePage({ style: { navigationStyle: 'custom', navigationBarTitleText: '订单' } })

const tokenStore = useTokenStore()
const orders = ref<LoveOrder[]>([])
const activeTab = ref<string>('all')
const loading = ref(false)

const tabs = [
  { label: '全部', value: 'all' },
  { label: '等确认', value: 'pending' },
  { label: '烹饪中', value: 'cooking' },
  { label: '已完成', value: 'finished' },
]

async function loadOrders() {
  if (!tokenStore.updateNowTime().hasLogin) {
    orders.value = []
    return
  }
  const status = activeTab.value === 'all' ? undefined : activeTab.value
  loading.value = true
  try { orders.value = await getOrders(status) || [] }
  catch { orders.value = [] }
  finally { loading.value = false }
}

function switchTab(val: string) {
  activeTab.value = val
  loadOrders()
}

function goLogin() {
  uni.navigateTo({ url: `/pages/auth/login?redirect=${encodeURIComponent('/pages/order/list')}` })
}

async function changeStatus(order: LoveOrder, status: OrderStatus) {
  try {
    await updateOrderStatus(order.id, status)
    uni.showToast({ title: '状态已更新', icon: 'success' })
    await loadOrders()
  }
  catch { /* interceptor toast */ }
}

function nextAction(order: LoveOrder) {
  if (order.status === 'pending') return { text: '确认接单', status: 'confirmed' as OrderStatus }
  if (order.status === 'confirmed') return { text: '开始烹饪', status: 'cooking' as OrderStatus }
  if (order.status === 'cooking') return { text: '做好啦', status: 'finished' as OrderStatus }
  return null
}

setOrderSocketMessageHandler(() => loadOrders())
onShow(() => { loadOrders() })
</script>

<template>
  <view class="order-page love-page min-h-screen">
    <view class="love-hero warm-gradient pt-safe px-5 pb-5" style="padding-top: calc(env(safe-area-inset-top) + 52rpx);">
      <view class="relative z-1">
        <view class="love-pill mb-3">🍱 我们的点单记录</view>
        <view class="text-6.5 font-800 text-#4a3728">每一单都是想念</view>
        <view class="text-3.2 text-#8b7355 mt-2">查看 TA 想吃什么，也记录你们一起吃过的每顿饭。</view>
      </view>
    </view>

    <view class="px-4 py-3 bg-#fffaf7">
      <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
        <view class="flex gap-3">
          <view v-for="tab in tabs" :key="tab.value" class="tab-item" :class="activeTab === tab.value ? 'active' : ''" @tap="switchTab(tab.value)">
            {{ tab.label }}
          </view>
        </view>
      </scroll-view>
    </view>

    <scroll-view scroll-y class="px-4 py-3" style="height: calc(100vh - 360rpx);">
      <view v-if="!tokenStore.hasLogin" class="empty-state love-card">
        <view class="text-8 mb-3">🔐</view>
        <view class="text-4 font-700 text-#4a3728 mb-2">登录后查看订单</view>
        <view class="text-3 text-#a08c7a mb-4">你们的小纸条和点单记录都在这里。</view>
        <view class="love-chip inline-flex" @tap="goLogin">去登录</view>
      </view>
      <view v-else-if="orders.length === 0" class="empty-state love-card">
        <view class="text-8 mb-3">📋</view>
        <view class="text-4 font-700 text-#4a3728 mb-2">暂无订单</view>
        <view class="text-3 text-#a08c7a">去厨房点一道今天想吃的吧。</view>
      </view>

      <view v-for="order in orders" :key="order.id" class="order-card love-card mb-3 p-4">
        <view class="flex items-center justify-between mb-3">
          <text class="text-3 text-#a08c7a">爱心单 #{{ String(order.id).slice(-8) }}</text>
          <view class="status-pill" :style="{ background: `${OrderStatusColor[order.status]}18`, color: OrderStatusColor[order.status] }">
            {{ OrderStatusText[order.status] || order.status }}
          </view>
        </view>

        <view v-for="item in order.items" :key="item.dishId" class="flex items-center gap-3 py-2">
          <view v-if="item.dishImage" class="w-14 h-14 rounded-4 shrink-0 overflow-hidden bg-#f0ebe6"><image :src="item.dishImage" mode="aspectFill" class="w-full h-full" /></view>
          <view v-else class="w-14 h-14 rounded-4 shrink-0 bg-#fff5f0 center text-8">🍽️</view>
          <view class="flex-1 min-w-0">
            <text class="text-3.5 font-600 text-#4a3728 block truncate">{{ item.dishName }}</text>
            <text class="text-2.8 text-#a08c7a">x{{ item.quantity }}</text>
          </view>
          <text class="price text-3.5 shrink-0">¥{{ (item.price * item.quantity).toFixed(2) }}</text>
        </view>

        <view v-if="order.loveNote" class="love-note mt-3">💌 {{ order.loveNote }}</view>

        <view class="flex items-center justify-between mt-3 pt-3 border-t border-#f0ebe6">
          <text class="text-3 text-#a08c7a">{{ order.createTime }}</text>
          <text class="text-4 font-800 price">¥{{ Number(order.totalAmount).toFixed(2) }}</text>
        </view>

        <view v-if="nextAction(order)" class="flex justify-end mt-3">
          <view class="action-next" @tap="changeStatus(order, nextAction(order)!.status)">{{ nextAction(order)!.text }}</view>
        </view>
      </view>
      <view class="h-8" />
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
.tabs-scroll { white-space: nowrap; }
.tab-item { padding: 16rpx 30rpx; border-radius: 999rpx; background: #fff; color: #8b7355; font-size: 28rpx; box-shadow: 0 8rpx 20rpx rgba(126,74,45,.05); }
.tab-item.active { background: #e85d3a; color: #fff; font-weight: 800; }
.status-pill { padding: 8rpx 18rpx; border-radius: 999rpx; font-size: 24rpx; font-weight: 700; }
.love-note { padding: 18rpx 20rpx; border-radius: 22rpx; background: #fff7f1; color: #e85d3a; font-size: 26rpx; line-height: 1.5; }
.action-next { padding: 14rpx 24rpx; border-radius: 999rpx; background: #fff1ec; color: #e85d3a; font-weight: 700; font-size: 26rpx; }
</style>
