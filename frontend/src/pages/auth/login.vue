<script lang="ts" setup>
import { useTokenStore } from '@/store/token'
import { connectOrderSocket } from '@/utils/ws'

const tokenStore = useTokenStore()
const form = reactive({
  username: 'lover',
  password: '123456',
})
const loading = ref(false)
const redirect = ref('/pages/index/index')

onLoad((options: Record<string, string | undefined>) => {
  if (options?.redirect) redirect.value = decodeURIComponent(options.redirect)
})

async function doLogin() {
  if (!form.username.trim()) return uni.showToast({ title: '请输入用户名', icon: 'none' })
  if (!form.password) return uni.showToast({ title: '请输入密码', icon: 'none' })

  loading.value = true
  try {
    await tokenStore.login({ username: form.username.trim(), password: form.password })
    connectOrderSocket()
    goAfterLogin()
  }
  catch {
    // 请求拦截器会提示错误
  }
  finally {
    loading.value = false
  }
}

function useDemo(username: 'chef' | 'lover') {
  form.username = username
  form.password = '123456'
}

function goAfterLogin() {
  const url = redirect.value || '/pages/index/index'
  const tabPages = ['/pages/index/index', '/pages/order/list', '/pages/discover/index', '/pages/me/me']
  if (tabPages.includes(url.split('?')[0])) uni.switchTab({ url })
  else uni.redirectTo({ url })
}
</script>

<template>
  <view class="login-page love-page min-h-screen px-6">
    <view class="pt-safe" style="padding-top: calc(env(safe-area-inset-top) + 80rpx);">
      <view class="love-hero warm-gradient p-6 mb-6">
        <view class="avatar-pair mb-5">
          <view class="avatar-pair__item center text-7">👨‍🍳</view>
          <view class="avatar-pair__item center text-7">💗</view>
          <view class="avatar-pair__item center text-7">👩</view>
        </view>
        <view class="text-7 font-800 text-#4a3728 leading-tight">欢迎回到<br>我们的私房厨房</view>
        <view class="text-3.5 text-#8b7355 mt-3">登录后点一道想吃的菜，把今天也过得热气腾腾。</view>
      </view>

      <view class="love-card p-5">
        <view class="mb-4">
          <text class="text-3 text-#a08c7a block mb-2">用户名</text>
          <view class="form-input">
            <input v-model="form.username" placeholder="请输入用户名" placeholder-style="color:#c9b8aa" />
          </view>
        </view>
        <view class="mb-5">
          <text class="text-3 text-#a08c7a block mb-2">密码</text>
          <view class="form-input">
            <input v-model="form.password" password placeholder="请输入密码" placeholder-style="color:#c9b8aa" />
          </view>
        </view>

        <wd-button block size="large" type="primary" :loading="loading" custom-style="background:#e85d3a;border-radius:28rpx;height:92rpx;font-size:30rpx;" @click="doLogin">
          进入厨房
        </wd-button>

        <view class="mt-5 p-4 rounded-6 bg-#fff7f1">
          <view class="text-3 font-700 text-#4a3728 mb-3">Demo 账号</view>
          <view class="flex gap-3">
            <view class="demo-chip" @tap="useDemo('lover')">lover / 123456</view>
            <view class="demo-chip" @tap="useDemo('chef')">chef / 123456</view>
          </view>
          <view class="text-2.8 text-#a08c7a mt-3">先用 lover 点餐，再用 chef 查看对方订单和状态通知。</view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.login-page {
  box-sizing: border-box;
}
.form-input {
  height: 88rpx;
  border-radius: 24rpx;
  background: #fff8f4;
  border: 1rpx solid #f0ebe6;
  padding: 0 28rpx;
  display: flex;
  align-items: center;

  input {
    width: 100%;
    color: #4a3728;
    font-size: 30rpx;
  }
}
.demo-chip {
  flex: 1;
  text-align: center;
  padding: 18rpx 12rpx;
  border-radius: 999rpx;
  color: #e85d3a;
  background: #fff;
  font-size: 24rpx;
  font-weight: 700;
  box-shadow: 0 8rpx 20rpx rgba(126, 74, 45, 0.06);
}
</style>
