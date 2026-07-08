import type { IUserInfoRes } from '@/api/types/login'
import { http } from '@/http/http'

/** 获取当前用户信息 */
export function getProfile() {
  return http.get<IUserInfoRes>('/user/info')
}

/** 更新用户信息 */
export function updateProfile(data: Partial<IUserInfoRes>) {
  return http.put('/user/info', data)
}

/** 情侣绑定 */
export function bindPartner(partnerId: number) {
  return http.post('/user/bind', { partnerId })
}
