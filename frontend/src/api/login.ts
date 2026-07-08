import type { LoveUser } from '@/types/user'
import { http } from '@/http/http'

export interface ILoginForm {
  username: string
  password: string
}

export interface ISingleTokenRes {
  token: string
  expiresIn: number
}

export type IAuthLoginRes = ISingleTokenRes

export type IUserInfoRes = LoveUser

/** 用户登录 */
export function login(loginForm: ILoginForm) {
  return http.post<IAuthLoginRes>('/auth/login', loginForm)
}

/** 获取用户信息 */
export function getUserInfo() {
  return http.get<LoveUser>('/user/info')
}

/** 退出登录 */
export function logout() {
  return http.get<void>('/auth/logout')
}

/** 获取微信登录凭证，后续完整接入微信登录时复用 */
export function getWxCode() {
  return new Promise<UniApp.LoginRes>((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: res => resolve(res),
      fail: err => reject(new Error(err.errMsg || '微信登录失败')),
    })
  })
}

/** 微信登录：当前 MVP 保留后端接口，主流程仍使用账号登录 */
export function wxLogin(data: { code: string }) {
  return http.post<IAuthLoginRes>('/auth/wxLogin', data)
}
