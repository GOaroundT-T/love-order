import type { LoveUser, UpdateProfileParams } from '@/types/user'
import { http } from '@/http/http'

/** 获取当前用户信息 */
export function getProfile() {
  return http.get<LoveUser>('/user/info')
}

/** 更新用户信息 */
export function updateProfile(data: UpdateProfileParams) {
  return http.put<void>('/user/info', data)
}

/** 情侣绑定 */
export function bindPartner(partnerId: number) {
  return http.post<void>('/user/bind', { partnerId })
}
