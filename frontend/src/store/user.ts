import type { LoveUser } from '@/types/user'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getUserInfo } from '@/api/login'

const userInfoState: LoveUser = {
  userId: -1,
  username: '',
  nickname: '',
  avatar: '/static/images/default-avatar.png',
  role: 'girlfriend',
  kitchenName: '我们的厨房',
  signature: '把每一餐都做成小小的约会 💗',
  partnerId: null,
}

export const useUserStore = defineStore(
  'user',
  () => {
    const userInfo = ref<LoveUser>({ ...userInfoState })

    const hasPartner = computed(() => !!userInfo.value.partnerId)
    const displayName = computed(() => userInfo.value.nickname || userInfo.value.username || '未登录')

    const setUserInfo = (val: LoveUser) => {
      userInfo.value = {
        ...userInfoState,
        ...val,
        avatar: val.avatar || userInfoState.avatar,
      }
    }

    const setUserAvatar = (avatar: string) => {
      userInfo.value.avatar = avatar
    }

    const clearUserInfo = () => {
      userInfo.value = { ...userInfoState }
      uni.removeStorageSync('user')
    }

    const fetchUserInfo = async () => {
      const res = await getUserInfo()
      setUserInfo(res)
      return res
    }

    return {
      userInfo,
      hasPartner,
      displayName,
      clearUserInfo,
      fetchUserInfo,
      setUserInfo,
      setUserAvatar,
    }
  },
  { persist: true },
)
