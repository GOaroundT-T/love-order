<script lang="ts" setup>
import type { LoveOrder, OrderStatus } from '@/types/dish'
import { OrderStatusColor, OrderStatusText } from '@/types/dish'
import { getOrders } from '@/api/order'
import { onShow } from '@dcloudio/uni-app'

defineOptions({ name: 'OrderList' })
definePage({ style: { navigationBarTitleText: '订单', navigationBarBackgroundColor: '#fff' } })

const orders = ref<LoveOrder[]>([])
const activeTab = ref<string>('all')

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待确认', value: 'pending' },
  { label: '烹饪中', value: 'cooking' },
  { label: '已完成', value: 'finished' },
]

async function loadOrders() {
  const status = activeTab.value === 'all' ? undefined : activeTab.value
  try {
    orders.value = await getOrders(status) || []
  }
  catch { orders.value = [] }
}

function switchTab(val: string) {
  activeTab.value = val
  loadOrders()
}

onShow(() => { loadOrders() })
</script>

<template>
  <view class="order-page bg-#fef9f5 min-h-screen pt-safe">
    <!-- 状态筛选 tabs -->
    <view class="bg-white px-4 py-3 border-b border-#f0ebe6">
      <view class="flex gap-3">
        <view
          v-for="tab in tabs" :key="tab.value"
          class="tab-item text-3.5 px-5 py-2 rounded-full"
          :class="activeTab === tab.value ? 'bg-#fef0ec text-#e85d3a font-600' : 'bg-#f5f2ee text-#8b7355'"
          @tap="switchTab(tab.value)"
        >
          {{ tab.label }}
        </view>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view scroll-y class="px-4 py-3" style="height: calc(100vh - 200rpx);">
      <view v-if="orders.length === 0" class="center pt-30">
        <view class="text-8 mb-3">📋</view>
        <text class="text-3.5 text-#b8a99a">暂无订单</text>
      </view>

      <view v-for="order in orders" :key="order.id" class="card mb-3 p-4">
        <view class="flex items-center justify-between mb-3">
          <text class="text-3 text-#a08c7a">订单号 #{{ String(order.id).slice(-8) }}</text>
          <view class="px-3 py-1 rounded-full text-2.5" :style="{ background: OrderStatusColor[order.status as OrderStatus] + '18', color: OrderStatusColor[order.status as OrderStatus] }">
            {{ OrderStatusText[order.status as OrderStatus] || order.status }}
          </view>
        </view>

        <view v-for="item in order.items" :key="item.dishId" class="flex items-center gap-3 py-2">
          <view v-if="item.dishImage" class="w-14 h-14 rounded-lg shrink-0 overflow-hidden bg-#f0ebe6">
            <image :src="item.dishImage" mode="aspectFill" class="w-full h-full" />
          </view>
          <view v-else class="w-14 h-14 rounded-lg shrink-0 bg-#fff5f0 center text-8">
            🍽️
          </view>
          <view class="flex-1 min-w-0">
            <text class="text-3.5 text-#4a3728 block truncate">{{ item.dishName }}</text>
          </view>
          <text class="text-3 text-#a08c7a shrink-0">x{{ item.quantity }}</text>
          <text class="price text-3.5 shrink-0">¥{{ (item.price * item.quantity).toFixed(2) }}</text>
        </view>

        <view v-if="order.loveNote" class="mt-2 text-2.5 text-#e85d3a">
          💕 {{ order.loveNote }}
        </view>

        <view class="flex items-center justify-between mt-3 pt-3 border-t border-#f0ebe6">
          <text class="text-3 text-#a08c7a">{{ order.createTime }}</text>
          <text class="text-4 font-700 price">¥{{ order.totalAmount.toFixed(2) }}</text>
        </view>
      </view>

      <view class="h-6" />
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
.tab-item {
  transition: all 0.2s;
}
</style>
