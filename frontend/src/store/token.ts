import type { ILoginForm, IAuthLoginRes } from '@/api/login'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getWxCode, login as _login, logout as _logout, wxLogin as _wxLogin } from '@/api/login'
import { useUserStore } from './user'

const tokenInfoState: IAuthLoginRes = {
  token: '',
  expiresIn: 0,
}

export const useTokenStore = defineStore(
  'token',
  () => {
    const tokenInfo = ref<IAuthLoginRes>({ ...tokenInfoState })
    const nowTime = ref(Date.now())

    const updateNowTime = () => {
      nowTime.value = Date.now()
      return useTokenStore()
    }

    const setTokenInfo = (val: IAuthLoginRes) => {
      updateNowTime()
      tokenInfo.value = val
      const expireTime = Date.now() + val.expiresIn * 1000
      uni.setStorageSync('accessTokenExpireTime', expireTime)
    }

    const isTokenExpired = computed(() => {
      if (!tokenInfo.value.token) return true
      const expireTime = uni.getStorageSync('accessTokenExpireTime')
      return !expireTime || nowTime.value >= expireTime
    })

    const validToken = computed(() => {
      if (isTokenExpired.value) return ''
      return tokenInfo.value.token
    })

    const hasLogin = computed(() => !!tokenInfo.value.token && !isTokenExpired.value)

    async function postLogin(res: IAuthLoginRes) {
      setTokenInfo(res)
      const userStore = useUserStore()
      await userStore.fetchUserInfo()
    }

    const login = async (loginForm: ILoginForm) => {
      const res = await _login(loginForm)
      await postLogin(res)
      uni.showToast({ title: '欢迎回家', icon: 'success' })
      return res
    }

    const wxLogin = async () => {
      const code = await getWxCode()
      const res = await _wxLogin({ code: code.code })
      await postLogin(res)
      uni.showToast({ title: '欢迎回家', icon: 'success' })
      return res
    }

    const logout = async () => {
      try {
        if (tokenInfo.value.token) await _logout()
      }
      catch (error) {
        console.error('退出登录失败:', error)
      }
      finally {
        updateNowTime()
        tokenInfo.value = { ...tokenInfoState }
        uni.removeStorageSync('accessTokenExpireTime')
        uni.removeStorageSync('refreshTokenExpireTime')
        uni.removeStorageSync('token')
        const userStore = useUserStore()
        userStore.clearUserInfo()
      }
    }

    const refreshToken = async () => {
      throw new Error('当前项目使用单 token 登录，暂不支持刷新 token')
    }

    const tryGetValidToken = async () => {
      updateNowTime()
      return validToken.value
    }

    return {
      login,
      wxLogin,
      logout,
      hasLogin,
      refreshToken,
      tryGetValidToken,
      validToken,
      tokenInfo,
      setTokenInfo,
      updateNowTime,
    }
  },
  { persist: true },
)
