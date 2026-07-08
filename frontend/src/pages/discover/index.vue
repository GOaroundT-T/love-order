<script lang="ts" setup>
defineOptions({ name: 'Discover' })
definePage({ style: { navigationBarTitleText: '发现', navigationBarBackgroundColor: '#fff' } })
</script>

<template>
  <view class="discover-page bg-#fef9f5 min-h-screen pt-safe p-4">
    <view class="center pt-30 flex flex-col items-center gap-4">
      <view class="text-8">🌸</view>
      <text class="text-5 font-700 text-#4a3728">发现页</text>
      <text class="text-3.5 text-#a08c7a text-center">
        这里会放推荐菜品、收藏、<br>美食日记等内容
      </text>
      <view class="card p-6 mt-6 w-full text-center">
        <view class="text-6 mb-2">👨‍🍳</view>
        <text class="text-3.5 text-#8b7355">他总是说：你点的我都做</text>
      </view>
      <view class="card p-6 mt-3 w-full text-center">
        <view class="text-6 mb-2">💝</view>
        <text class="text-3.5 text-#8b7355">每次下厨都是我们的小浪漫</text>
      </view>
    </view>
  </view>
</template>
