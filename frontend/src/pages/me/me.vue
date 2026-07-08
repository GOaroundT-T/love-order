<script lang="ts" setup>
import { bindPartner, getProfile, updateProfile } from '@/api/user'
import { useTokenStore } from '@/store/token'
import { useUserStore } from '@/store/user'
import { closeOrderSocket } from '@/utils/ws'

defineOptions({ name: 'Me' })
definePage({ style: { navigationStyle: 'custom', navigationBarTitleText: '我的' } })

const tokenStore = useTokenStore()
const userStore = useUserStore()
const user = computed(() => userStore.userInfo)
const profileVisible = ref(false)
const bindVisible = ref(false)
const saving = ref(false)
const binding = ref(false)
const partnerIdText = ref('')
const profileForm = reactive({
  nickname: '',
  kitchenName: '',
  signature: '',
})

const roleText = computed(() => user.value.role === 'chef' ? '专属大厨' : '幸福点单人')
const bindText = computed(() => user.value.partnerId ? `已和 #${user.value.partnerId} 绑定` : '还没绑定另一半')

async function loadProfile() {
  if (!tokenStore.updateNowTime().hasLogin) return
  try {
    const res = await getProfile()
    userStore.setUserInfo(res)
  }
  catch { /* empty */ }
}

function ensureLogin() {
  if (tokenStore.updateNowTime().hasLogin) return true
  uni.navigateTo({ url: `/pages/auth/login?redirect=${encodeURIComponent('/pages/me/me')}` })
  return false
}

function openProfile() {
  if (!ensureLogin()) return
  profileForm.nickname = user.value.nickname || ''
  profileForm.kitchenName = user.value.kitchenName || ''
  profileForm.signature = user.value.signature || ''
  profileVisible.value = true
}

async function saveProfile() {
  saving.value = true
  try {
    await updateProfile({ ...profileForm })
    await loadProfile()
    profileVisible.value = false
    uni.showToast({ title: '资料已更新', icon: 'success' })
  }
  catch { /* interceptor toast */ }
  finally { saving.value = false }
}

function openBind() {
  if (!ensureLogin()) return
  partnerIdText.value = user.value.partnerId ? String(user.value.partnerId) : ''
  bindVisible.value = true
}

async function submitBind() {
  const partnerId = Number(partnerIdText.value)
  if (!partnerId) return uni.showToast({ title: '请输入对方用户ID', icon: 'none' })
  binding.value = true
  try {
    await bindPartner(partnerId)
    await loadProfile()
    bindVisible.value = false
    uni.showToast({ title: '情侣绑定成功', icon: 'success' })
  }
  catch { /* interceptor toast */ }
  finally { binding.value = false }
}

function goOrders() {
  if (!ensureLogin()) return
  uni.switchTab({ url: '/pages/order/list' })
}

async function doLogout() {
  await tokenStore.logout()
  closeOrderSocket()
  uni.showToast({ title: '已退出登录', icon: 'none' })
}

onShow(() => { loadProfile() })
</script>

<template>
  <view class="me-page love-page min-h-screen">
    <view class="love-hero warm-gradient pt-safe pb-7 px-6" style="padding-top: calc(env(safe-area-inset-top) + 56rpx);">
      <view class="flex items-center gap-4 relative z-1">
        <image :src="user.avatar || '/static/images/default-avatar.png'" mode="aspectFill" class="avatar-main" />
        <view class="flex-1 min-w-0">
          <view class="love-pill mb-2">{{ roleText }}</view>
          <text class="text-5.5 font-800 text-#4a3728 block truncate">{{ user.nickname || '未登录' }}</text>
          <text class="text-3 text-#8b7355 mt-1 block">@{{ user.username || '请先登录' }}</text>
        </view>
        <view class="setting-btn center" @tap="openProfile">
          <wd-icon name="setting1" size="18px" color="#e85d3a" />
        </view>
      </view>
      <view class="profile-card relative z-1 mt-5">
        <view>
          <view class="text-3 text-#a08c7a">{{ user.kitchenName || '我们的厨房' }}</view>
          <view class="text-3.5 font-700 text-#4a3728 mt-1">{{ user.signature || '把每一餐都做成小小的约会 💗' }}</view>
        </view>
        <view class="love-chip">ID: {{ user.userId > 0 ? user.userId : '-' }}</view>
      </view>
    </view>

    <view class="px-4 py-4">
      <view class="love-card p-4 mb-4">
        <view class="flex items-center justify-between">
          <view>
            <view class="text-4 font-800 text-#4a3728">情侣绑定</view>
            <view class="text-3 text-#a08c7a mt-1">{{ bindText }}</view>
          </view>
          <view class="love-chip" @tap="openBind">去绑定</view>
        </view>
      </view>

      <view class="love-card overflow-hidden">
        <view class="cell" @tap="goOrders">
          <view class="flex items-center gap-2">
            <wd-icon name="orders" size="18px" color="#e85d3a" />
            <text>我们的订单</text>
          </view>
          <wd-icon name="arrow" size="14px" color="#ccc" />
        </view>
        <view class="cell" @tap="openProfile">
          <view class="flex items-center gap-2">
            <wd-icon name="edit-1" size="18px" color="#f0ad4e" />
            <text>厨房资料</text>
          </view>
          <wd-icon name="arrow" size="14px" color="#ccc" />
        </view>
        <view class="cell">
          <view class="flex items-center gap-2">
            <wd-icon name="heart" size="18px" color="#ff7f9b" />
            <text>收藏与日记</text>
          </view>
          <text class="text-2.8 text-#c0b1a5">即将上线</text>
        </view>
      </view>

      <wd-button v-if="tokenStore.hasLogin" block plain type="info" custom-style="margin-top:32rpx;border-radius:28rpx;height:88rpx;color:#d14a28;border-color:#f5c2b0;" @click="doLogout">
        退出登录
      </wd-button>
      <wd-button v-else block type="primary" custom-style="margin-top:32rpx;background:#e85d3a;border-radius:28rpx;height:88rpx;" @click="ensureLogin">
        去登录
      </wd-button>
    </view>

    <wd-popup v-model="profileVisible" position="bottom" custom-style="border-radius: 32rpx 32rpx 0 0;">
      <view class="popup-panel">
        <view class="text-4.5 font-800 text-#4a3728 mb-4">编辑我们的厨房</view>
        <view class="form-item"><text>昵称</text><input v-model="profileForm.nickname" placeholder="你的昵称" /></view>
        <view class="form-item"><text>厨房名</text><input v-model="profileForm.kitchenName" placeholder="比如：阿鱼的暖心厨房" /></view>
        <view class="form-item"><text>签名</text><textarea v-model="profileForm.signature" auto-height :maxlength="80" placeholder="写一句温柔的话" /></view>
        <wd-button block type="primary" :loading="saving" custom-style="background:#e85d3a;border-radius:28rpx;height:88rpx;margin-top:24rpx;" @click="saveProfile">保存</wd-button>
      </view>
    </wd-popup>

    <wd-popup v-model="bindVisible" position="bottom" custom-style="border-radius: 32rpx 32rpx 0 0;">
      <view class="popup-panel">
        <view class="text-4.5 font-800 text-#4a3728">绑定另一半</view>
        <view class="text-3 text-#a08c7a mt-2 mb-4">把你的 ID 发给 TA，或输入 TA 的 ID。你的 ID：{{ user.userId }}</view>
        <view class="form-item"><text>对方用户ID</text><input v-model="partnerIdText" type="number" placeholder="请输入对方 ID" /></view>
        <wd-button block type="primary" :loading="binding" custom-style="background:#e85d3a;border-radius:28rpx;height:88rpx;margin-top:24rpx;" @click="submitBind">确认绑定</wd-button>
      </view>
    </wd-popup>
  </view>
</template>

<style lang="scss" scoped>
.avatar-main { width: 128rpx; height: 128rpx; border-radius: 999rpx; border: 6rpx solid #fff; background: #fff5f0; box-shadow: 0 12rpx 30rpx rgba(126,74,45,.1); }
.setting-btn { width: 72rpx; height: 72rpx; border-radius: 999rpx; background: rgba(255,255,255,.78); }
.profile-card { display: flex; justify-content: space-between; gap: 24rpx; padding: 24rpx; border-radius: 28rpx; background: rgba(255,255,255,.72); }
.cell { display: flex; align-items: center; justify-content: space-between; padding: 32rpx; color: #4a3728; font-size: 28rpx; border-bottom: 1rpx solid #f0ebe6; }
.cell:last-child { border-bottom: 0; }
.cell:active { background: #fff7f1; }
.popup-panel { padding: 40rpx 32rpx calc(40rpx + env(safe-area-inset-bottom)); background: #fffaf7; }
.form-item { margin-bottom: 24rpx; padding: 22rpx; border-radius: 24rpx; background: #fff; border: 1rpx solid #f0ebe6; }
.form-item text { display: block; color: #a08c7a; font-size: 24rpx; margin-bottom: 12rpx; }
.form-item input, .form-item textarea { width: 100%; color: #4a3728; font-size: 28rpx; line-height: 1.6; }
</style>
