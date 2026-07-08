<script lang="ts" setup>
import type { Dish } from '@/types/dish'
import { getDishes } from '@/api/dish'

defineOptions({ name: 'Discover' })
definePage({ style: { navigationStyle: 'custom', navigationBarTitleText: '发现' } })

const dishes = ref<Dish[]>([])
const dateIdeas = [
  { icon: '🍲', title: '雨天热汤局', desc: '一碗汤、两双筷子，把坏天气煮成好心情。' },
  { icon: '🎬', title: '追剧下饭夜', desc: '小炒 + 主食 + 冰饮，适合窝在沙发里慢慢吃。' },
  { icon: '🌙', title: '晚安甜汤', desc: '饭后留一点甜，今天就温柔收尾。' },
]
const loveNotes = [
  '“你点的我都做，但你要负责多吃一点。”',
  '“今天不想做大事，只想认真吃一顿饭。”',
  '“把想念放进锅里，出锅就是热乎乎的喜欢。”',
]
const topDishes = computed(() => dishes.value.slice(0, 3))

onShow(async () => {
  try { dishes.value = await getDishes() || [] }
  catch { dishes.value = [] }
})
</script>

<template>
  <view class="discover-page love-page min-h-screen">
    <view class="love-hero warm-gradient pt-safe px-5 pb-6" style="padding-top: calc(env(safe-area-inset-top) + 52rpx);">
      <view class="relative z-1">
        <view class="love-pill mb-3">🌸 情侣仪式感</view>
        <view class="text-7 font-800 text-#4a3728 leading-tight">今天也要<br>好好一起吃饭</view>
        <view class="text-3.5 text-#8b7355 mt-3">这里放一些约会菜单、想吃灵感和只属于你们的小浪漫。</view>
      </view>
    </view>

    <scroll-view scroll-y class="px-4 py-4" style="height: calc(100vh - 300rpx);">
      <view class="section-title">今日推荐</view>
      <view v-if="topDishes.length" class="recommend-row">
        <view v-for="dish in topDishes" :key="dish.id" class="recommend-card love-card">
          <view class="food-emoji">{{ dish.spicyLevel === 'hot' ? '🌶️' : '🍽️' }}</view>
          <view class="text-3.5 font-700 text-#4a3728 truncate">{{ dish.name }}</view>
          <view class="text-2.6 text-#a08c7a mt-1 truncate-2">{{ dish.description }}</view>
          <view class="price text-3.5 mt-2">¥{{ dish.price }}</view>
        </view>
      </view>
      <view v-else class="empty-state love-card">还没有推荐菜，先去厨房看看吧 🍳</view>

      <view class="section-title mt-6">约会菜单灵感</view>
      <view class="love-card overflow-hidden">
        <view v-for="idea in dateIdeas" :key="idea.title" class="idea-cell">
          <view class="idea-icon center">{{ idea.icon }}</view>
          <view class="flex-1 min-w-0">
            <view class="text-3.8 font-700 text-#4a3728">{{ idea.title }}</view>
            <view class="text-2.8 text-#a08c7a mt-1">{{ idea.desc }}</view>
          </view>
        </view>
      </view>

      <view class="section-title mt-6">小纸条收藏夹</view>
      <view v-for="note in loveNotes" :key="note" class="note-card love-card mb-3">
        <view class="text-5 mb-2">💌</view>
        <view class="text-3.2 text-#8b7355 leading-relaxed">{{ note }}</view>
      </view>

      <view class="h-8" />
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
.section-title { margin: 12rpx 4rpx 20rpx; color: #4a3728; font-size: 34rpx; font-weight: 800; }
.recommend-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 18rpx; }
.recommend-card { padding: 22rpx 18rpx; min-height: 210rpx; }
.food-emoji { width: 72rpx; height: 72rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; background: #fff5f0; font-size: 42rpx; margin-bottom: 14rpx; }
.idea-cell { display: flex; gap: 22rpx; padding: 28rpx; border-bottom: 1rpx solid #f0ebe6; }
.idea-cell:last-child { border-bottom: 0; }
.idea-icon { width: 76rpx; height: 76rpx; border-radius: 24rpx; background: #fff5f0; font-size: 40rpx; }
.note-card { padding: 28rpx; }
</style>
