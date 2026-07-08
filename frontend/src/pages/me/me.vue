<script lang="ts" setup>
import type { IUserInfoRes } from '@/api/types/login'
import { getProfile } from '@/api/user'

defineOptions({ name: 'Me' })
definePage({ style: { navigationStyle: 'custom', navigationBarTitleText: '我的' } })

const user = ref<Partial<IUserInfoRes>>({})

async function loadProfile() {
  try {
    user.value = await getProfile() || {}
  }
  catch { /* empty */ }
}

function onSetting() {
  uni.showToast({ title: '设置（开发中）', icon: 'none' })
}

onLoad(() => { loadProfile() })
</script>

<template>
  <view class="me-page bg-#fef9f5 min-h-screen">
    <!-- 顶部 -->
    <view class="warm-gradient pt-safe pb-8 px-6" style="padding-top: calc(env(safe-area-inset-top) + 60rpx);">
      <view class="flex items-center gap-4">
        <image
          :src="user.avatar || '/static/images/default-avatar.png'"
          mode="aspectFill"
          class="w-16 h-16 rounded-full border-3 border-white shadow-sm bg-#f0ebe6"
        />
        <view class="flex-1">
          <text class="text-5 font-700 text-#4a3728 block">{{ user.nickname || '未登录' }}</text>
          <text class="text-3 text-#a08c7a mt-1 block">{{ user.username || '-' }}</text>
        </view>
        <view class="flex items-center gap-1 text-3 text-#8b7355" @tap="onSetting">
          <wd-icon name="setting1" size="18px" color="#8b7355" />
          <text>设置</text>
        </view>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="px-4 py-4">
      <view class="card">
        <view class="cell flex items-center justify-between p-4 border-b border-#f0ebe6">
          <view class="flex items-center gap-2">
            <wd-icon name="orders" size="18px" color="#e85d3a" />
            <text class="text-3.5 text-#4a3728">我的订单</text>
          </view>
          <wd-icon name="arrow" size="14px" color="#ccc" />
        </view>
        <view class="cell flex items-center justify-between p-4 border-b border-#f0ebe6">
          <view class="flex items-center gap-2">
            <wd-icon name="star" size="18px" color="#f0ad4e" />
            <text class="text-3.5 text-#4a3728">我的收藏</text>
          </view>
          <wd-icon name="arrow" size="14px" color="#ccc" />
        </view>
        <view class="cell flex items-center justify-between p-4 border-b border-#f0ebe6">
          <view class="flex items-center gap-2">
            <wd-icon name="link" size="18px" color="#e85d3a" />
            <text class="text-3.5 text-#4a3728">情侣绑定</text>
          </view>
          <wd-icon name="arrow" size="14px" color="#ccc" />
        </view>
        <view class="cell flex items-center justify-between p-4">
          <view class="flex items-center gap-2">
            <wd-icon name="info-circle" size="18px" color="#a08c7a" />
            <text class="text-3.5 text-#4a3728">关于我们</text>
          </view>
          <wd-icon name="arrow" size="14px" color="#ccc" />
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.cell:active {
  background: #faf7f4;
}
.border-3 {
  border-width: 3rpx;
}
</style>
